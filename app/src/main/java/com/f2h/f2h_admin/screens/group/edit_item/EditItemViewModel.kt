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
import com.f2h.f2h_admin.network.GroupMembershipApi
import com.f2h.f2h_admin.network.ItemApi
import com.f2h.f2h_admin.network.UomApi
import com.f2h.f2h_admin.network.UserApi
import com.f2h.f2h_admin.network.models.Item
import com.f2h.f2h_admin.network.models.ItemUpdateRequest
import com.f2h.f2h_admin.network.models.Uom
import com.f2h.f2h_admin.network.models.UserDetails
import kotlinx.coroutines.*
import java.util.*


class EditItemViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    var selectedItemId = -1L

    val itemName = MutableLiveData<String>()
    val itemImageUrl = MutableLiveData<String>()
    val itemDescription = MutableLiveData<String>()
    val itemPrice = MutableLiveData<String>()
    val confirmQuantityJump = MutableLiveData<String>()
    val orderQuantityJump = MutableLiveData<String>()
    val imageFilePath = MutableLiveData<String>()

    val selectedItemUomDetails = MutableLiveData<Uom>()
    val selectedFarmerDetails = MutableLiveData<UserDetails>()
    val farmersStringList = MutableLiveData<List<String>>()
    val uomStringList = MutableLiveData<List<String>>()

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
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        getItemFarmersAndUoms()
    }

    private fun getItemFarmersAndUoms() {
        coroutineScope.launch {
            _sessionData.value = retrieveSession()
            val getAllUomsDataDeferred = UomApi.retrofitService.getAllUoms()
            val getAllFarmersInGroupDataDeferred = GroupMembershipApi.retrofitService.getGroupMembership(_sessionData.value!!.groupId, listOf("FARMER"))
            val getSelectedItemDataDeferred = ItemApi.retrofitService.getItem(selectedItemId)
            try {
                val farmerUserIds = getAllFarmersInGroupDataDeferred.await().map { it.userId ?: -1}
                val getFarmerDetailsDataDeferred = UserApi.retrofitService.getUserDetailsByUserIds(farmerUserIds)

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
        itemPrice.value = selectedItem.pricePerUnit.toString()
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
        if (!isDecimal(itemPrice.value.toString())) {
            _toastText.value = "Please enter a valid price per unit number"
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
        var requestBody = ItemUpdateRequest(
            itemName = itemName.value,
            groupId = _sessionData.value?.groupId,
            farmerUserId = selectedFarmerDetails.value?.userId,
            farmerUserName = selectedFarmerDetails.value?.userName,
            description = itemDescription.value,
            uom = selectedItemUomDetails.value?.uom,
            pricePerUnit = itemPrice.value?.toDouble(),
            confirmQtyJump = confirmQuantityJump.value?.toDouble(),
            orderQtyJump = orderQuantityJump.value?.toDouble(),
            updatedBy = _sessionData.value?.userName,
            imageLink = imageUrl
        )

        coroutineScope.launch {
            val updateItemDataDeferred =
                ItemApi.retrofitService.updateItemForGroup(selectedItemId, requestBody)
            try {
                updateItemDataDeferred.await()
                _toastText.value =
                    String.format("Successfully updated item, %s", selectedItem.itemName)
            } catch (t: Throwable) {
                _toastText.value = "Oops something went wrong, please try again"
            }
            _isProgressBarActive.value = false
        }
    }


    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}