package com.f2h.f2h_admin.screens.group.edit_item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.f2h.f2h_admin.BuildConfig
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.*
import com.f2h.f2h_admin.screens.group.edit_item.HandlingChargesItemsModel
import kotlinx.coroutines.*
import java.util.*


class EditItemViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var selectedItemId = -1L

    val itemName = MutableLiveData<String>()
    val itemImageUrl = MutableLiveData<String>()
    val itemDescription = MutableLiveData<String>()
    val farmerPrice = MutableLiveData<String>()
    val v2Price = MutableLiveData<String>()
    val confirmQuantityJump = MutableLiveData<String>()
    val orderQuantityJump = MutableLiveData<String>()
    val imageFilePath = MutableLiveData<String>()

    val selectedItemUomDetails = MutableLiveData<Uom>()
    val selectedFarmerDetails = MutableLiveData<UserDetails>()
    val farmersStringList = MutableLiveData<List<String>>()
    val uomStringList = MutableLiveData<List<String>>()

    private var _visibleHandlingChargeUiData = MutableLiveData<MutableList<HandlingChargesItemsModel>>()
    val visibleHandlingChargeUiData: LiveData<MutableList<HandlingChargesItemsModel>>
        get() = _visibleHandlingChargeUiData

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private val _sessionData = MutableLiveData<SessionEntity>()

    private var selectedItem = Item()
    private var farmerDetails = listOf<UserDetails>()
    private var itemUomDetails = listOf<Uom>()
    private var handlingOptions = listOf<HandlingOption>()
    private var handlingCharges = listOf<HandlingCharge>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        _visibleHandlingChargeUiData.value = arrayListOf()
        getHandlingChargesAndOptions()
        getItemFarmersAndUoms()
    }


    private fun getHandlingChargesAndOptions() {
        coroutineScope.launch {
            try {
                val getAllHandlingOptionsDataDeferred = HandlingOptionApi.retrofitService(getApplication()).getAllHandlingOptions()
                val getAllHandlingChargesDataDeferred = ItemApi.retrofitService(getApplication()).getAllHandlingChargesForItem(selectedItemId)
                handlingOptions = getAllHandlingOptionsDataDeferred.await()
                handlingCharges = getAllHandlingChargesDataDeferred.await()
                populateHandlingChargeRecyclerView()
            } catch (t:Throwable){
                println(t.message)
            }
        }
    }

    private fun populateHandlingChargeRecyclerView() {
        handlingCharges.forEach { charge ->
            var uiModel = HandlingChargesItemsModel()
            uiModel.handlingOptionId = charge.handlingOptionId
            uiModel.handlingChargeId = charge.handlingChargeId
            uiModel.name = charge.name
            uiModel.description = charge.description
            uiModel.isItemChecked = true
            uiModel.handlingCharge = charge.amount
            _visibleHandlingChargeUiData.value?.add(uiModel)
        }

        var includedOptions = handlingCharges.map { it.handlingOptionId }
        handlingOptions.filter { it.handlingOptionId !in includedOptions }
            .forEach { option ->
                var uiModel = HandlingChargesItemsModel()
                uiModel.handlingOptionId = option.handlingOptionId
                uiModel.handlingChargeId =  0
                uiModel.name = option.name
                uiModel.description = option.description
                uiModel.isItemChecked = false
                uiModel.handlingCharge = 0.0
                _visibleHandlingChargeUiData.value?.add(uiModel)
            }
        _visibleHandlingChargeUiData.value = _visibleHandlingChargeUiData.value
    }


    private fun getItemFarmersAndUoms() {
        coroutineScope.launch {
            _sessionData.value = retrieveSession()
            val getAllUomsDataDeferred = UomApi.retrofitService(getApplication()).getAllUoms()
            val getAllFarmersInGroupDataDeferred = GroupMembershipApi.retrofitService(getApplication()).getGroupMembership(_sessionData.value!!.groupId, "FARMER")
            val getSelectedItemDataDeferred = ItemApi.retrofitService(getApplication()).getItem(selectedItemId)
            try {
                val farmerUserIds = getAllFarmersInGroupDataDeferred.await().map { it.userId ?: -1}
                val getFarmerDetailsDataDeferred = UserApi.retrofitService(getApplication()).getUserDetailsByUserIds(farmerUserIds.joinToString())

                selectedItem = getSelectedItemDataDeferred.await()
                farmerDetails = getFarmerDetailsDataDeferred.await()
                itemUomDetails =  getAllUomsDataDeferred.await()

                createSpinnerEntries()
                setItemDetailsForUI()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }

    private fun setItemDetailsForUI(){
        //Populate the UI screen (edit texts with item details
        itemName.value = selectedItem.itemName
        itemDescription.value = selectedItem.description
        farmerPrice.value = (selectedItem.farmerPrice ?: 0.0).toString()
        v2Price.value = (selectedItem.v2Price ?: 0.0).toString()
        itemImageUrl.value = selectedItem.imageLink
        confirmQuantityJump.value = selectedItem.confirmQtyJump.toString()
        orderQuantityJump.value = selectedItem.orderQtyJump.toString()
    }


    private fun createSpinnerEntries() {
        uomStringList.value = arrayListOf(selectedItem.uom ?: "").plus(itemUomDetails
            .map { it.uom ?: "" }
            .minus(selectedItem.uom ?: ""))

        val selectedFarmerData = farmerDetails.filter { it.userId?.equals(selectedItem.farmerUserId) ?: false }.first()
        val farmerFilterName = generateUniqueFilterName(selectedFarmerData.userName ?: "", selectedFarmerData.mobile.toString())
        farmersStringList.value = arrayListOf(farmerFilterName).plus(farmerDetails
                .map { generateUniqueFilterName(it.userName ?: "", it.mobile.toString()) }
                .minus(farmerFilterName))
    }


    private fun generateUniqueFilterName(name: String, mobile: String): String{
        return String.format("%s (%s)",name, mobile)
    }


    private suspend fun retrieveSession() : SessionEntity {
        return withContext(Dispatchers.IO) {
            val sessions = database.getAll()
            var session = SessionEntity()
            if (sessions.size==1) {
                session = sessions[0]
                println(session.toString())
            } else {
                database.clearSessions()
            }
            return@withContext session
        }
    }


    fun onItemFarmerSelected(position: Int) {
        selectedFarmerDetails.value = farmerDetails.filter { generateUniqueFilterName(it.userName ?: "", it.mobile ?: "")
            .equals(farmersStringList.value?.get(position).toString()) }
            .firstOrNull()
    }

    fun onItemUomSelected(position: Int) {
        selectedItemUomDetails.value = itemUomDetails.filter { it.uom
            .equals(uomStringList.value?.get(position).toString()) }
            .firstOrNull()
    }


    private fun isAnyFieldInvalid(): Boolean {

        if (itemName.value.isNullOrBlank()) {
            _toastText.value = "Please enter an Item Name"
            return true
        }
        if (itemDescription.value.isNullOrBlank()) {
            _toastText.value = "Please enter a description"
            return true
        }
        if (selectedFarmerDetails.value?.userName.isNullOrBlank()) {
            _toastText.value = "Please select a valid Farmer Name"
            return true
        }
        if (selectedItemUomDetails.value?.uom.isNullOrBlank()) {
            _toastText.value = "Please select a valid unit of measure"
            return true
        }
        if (!isDecimal(confirmQuantityJump.value.toString())) {
            _toastText.value = "Please enter a valid minimum confirm quantity number"
            return true
        }
        if (!isDecimal(orderQuantityJump.value.toString())) {
            _toastText.value = "Please enter a valid minimum order quantity number"
            return true
        }
        if (!isDecimal(farmerPrice.value.toString())) {
            _toastText.value = "Please enter a valid farmer price number"
            return true
        }

        return false
    }

    protected fun isDecimal(numberString: String): Boolean{
        try {
            val num = numberString.toDouble()
        } catch (e: Exception) {
            return false
        }
        return true;
    }



    fun onUpdateItemButtonClicked() {

        if(isAnyFieldInvalid()){
            return
        }

        if (!imageFilePath.value.isNullOrBlank()) {
            updateImageAndItemData()
            return
        }

        updateItemData(null)
    }

    private fun updateImageAndItemData() {
        _isProgressBarActive.value = true
        val requestId = MediaManager.get().upload(imageFilePath.value)
            .unsigned("unsigned_upload_settings")
            .option("public_id", String.format("itemId_%s__%s", selectedItemId, Calendar.getInstance().time))
            .option("folder", String.format("%s/group_%s/item_images", BuildConfig.ENVIRONMENT, _sessionData.value?.groupId))
            .option("tags", String.format("group_%s,item_%s,uploaderUserId_%s", _sessionData.value?.groupId, selectedItem.itemId,_sessionData.value?.userId))
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>?) {
                    println(resultData)
                    updateItemData(resultData?.get("url").toString())
                }

                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {
                    println(bytes / totalBytes)
                }

                override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                    TODO("Not yet implemented")
                }

                override fun onError(requestId: String, error: ErrorInfo) {
                    println(error)
                    _toastText.value = "Unable to upload image"
                    _isProgressBarActive.value = false
                }

                override fun onStart(requestId: String?) {
                }
            })
            .dispatch()
    }

    private fun updateItemData(imageUrl: String?) {
        _isProgressBarActive.value = true


        //Update existing handling charges and Item
        var requestBody = ItemUpdateRequest(
            itemName = itemName.value,
            groupId = _sessionData.value?.groupId,
            farmerUserId = selectedFarmerDetails.value?.userId,
            farmerUserName = selectedFarmerDetails.value?.userName,
            description = itemDescription.value,
            uom = selectedItemUomDetails.value?.uom,
            farmerPrice = farmerPrice.value?.toDouble(),
            v2Price = v2Price.value?.toDouble(),
            confirmQtyJump = confirmQuantityJump.value?.toDouble(),
            orderQtyJump = orderQuantityJump.value?.toDouble(),
            updatedBy = _sessionData.value?.userName,
            imageLink = imageUrl,
            handlingCharges = createHandlingChargeRequest()
        )

        coroutineScope.launch {
            val updateItemDataDeferred =
                ItemApi.retrofitService(getApplication()).updateItemForGroup(selectedItemId, requestBody)
            try {
                updateItemDataDeferred.await()
                _toastText.value =
                    String.format("Successfully updated item, %s", selectedItem.itemName)
            } catch (e: Exception) {
                _toastText.value = "Oops something went wrong, please try again"
            }
            _isProgressBarActive.value = false
        }
    }

    private fun createHandlingChargeRequest(): ArrayList<HandlingChargesCreateRequest> {
        var handlingChargesCreateRequests = ArrayList<HandlingChargesCreateRequest>()
        visibleHandlingChargeUiData.value
            ?.filter { it.isItemChecked.equals(true) }
            ?.forEach { handlingCharge ->
                var request = HandlingChargesCreateRequest()
                request.handlingOptionId = handlingCharge.handlingOptionId
                request.amount = handlingCharge.handlingCharge
                request.userVisibility = false
                request.createdBy = _sessionData.value?.userName ?: ""
                request.updatedBy = _sessionData.value?.userName ?: ""
                handlingChargesCreateRequests.add(request)
            }
        return handlingChargesCreateRequests
    }


    fun onCheckBoxClicked(selectedUiModel: HandlingChargesItemsModel) {
        var isChecked = visibleHandlingChargeUiData.value
            ?.filter { it.handlingOptionId.equals(selectedUiModel.handlingOptionId) }
            ?.first()
            ?.isItemChecked ?: true

        _visibleHandlingChargeUiData.value
            ?.filter { it.handlingOptionId.equals(selectedUiModel.handlingOptionId) }
            ?.first()
            ?.isItemChecked = !isChecked
    }



    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}