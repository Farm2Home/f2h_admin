package com.f2h.f2h_admin.screens.group.add_item

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.f2h.f2h_admin.database.SessionDatabaseDao
import com.f2h.f2h_admin.database.SessionEntity
import com.f2h.f2h_admin.network.*
import com.f2h.f2h_admin.network.models.ItemCreateRequest
import com.f2h.f2h_admin.network.models.Uom
import com.f2h.f2h_admin.network.models.UserDetails
import kotlinx.coroutines.*
import java.lang.Exception

class AddItemViewModel(val database: SessionDatabaseDao, application: Application) : AndroidViewModel(application) {

    val itemName = MutableLiveData<String>()
    val itemDescription = MutableLiveData<String>()
    val itemPrice = MutableLiveData<String>()
    val confirmQuantityJump = MutableLiveData<String>()
    val orderQuantityJump = MutableLiveData<String>()
    val selectedItemUomDetails = MutableLiveData<Uom>()
    val selectedFarmerDetails = MutableLiveData<UserDetails>()
    val farmersStringList = MutableLiveData<List<String>>()
    val itemUomStringList = MutableLiveData<List<String>>()

    private val _isProgressBarActive = MutableLiveData<Boolean>()
    val isProgressBarActive: LiveData<Boolean>
        get() = _isProgressBarActive

    private val _toastText = MutableLiveData<String>()
    val toastText: LiveData<String>
        get() = _toastText

    private val _sessionData = MutableLiveData<SessionEntity>()

    private var farmerDetails = listOf<UserDetails>()
    private var itemUomDetails = listOf<Uom>()
    private var viewModelJob = Job()
    private val coroutineScope = CoroutineScope(viewModelJob + Dispatchers.Main)


    init {
        _isProgressBarActive.value = true
        getFarmersAndUoms()
    }



    private fun getFarmersAndUoms() {
        coroutineScope.launch {
            _sessionData.value = retrieveSession()
            val getAllUomsDataDeferred = UomApi.retrofitService.getAllUoms()
            val getAllFarmersInGroupDataDeferred = GroupMembershipApi.retrofitService.getGroupMembership(_sessionData.value!!.groupId, listOf("FARMER"))
            try {
                val farmerUserIds = getAllFarmersInGroupDataDeferred.await().map { it.userId ?: -1}
                val getFarmerDetailsDataDeferred = UserApi.retrofitService.getUserDetailsByUserIds(farmerUserIds)
                farmerDetails = getFarmerDetailsDataDeferred.await()
                itemUomDetails =  getAllUomsDataDeferred.await()
                createSpinnerEntries()
            } catch (t:Throwable){
                println(t.message)
            }
            _isProgressBarActive.value = false
        }
    }

    private fun createSpinnerEntries() {
        itemUomStringList.value = itemUomDetails.map { it.uom ?: "" }
        farmersStringList.value =
            farmerDetails.map { generateUniqueFilterName(it.userName ?: "", it.mobile.toString()) }
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
            .equals(itemUomStringList.value?.get(position).toString()) }
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

    fun onAddItemButtonClicked() {

        if(isAnyFieldInvalid()){
            return
        }

        var requestBody = ItemCreateRequest(
            itemName = itemName.value,
            groupId = _sessionData.value?.groupId,
            farmerUserId = selectedFarmerDetails.value?.userId,
            farmerUserName = selectedFarmerDetails.value?.userName,
            description = itemDescription.value,
            uom = selectedItemUomDetails.value?.uom,
            pricePerUnit = itemPrice.value?.toDouble(),
            confirmQtyJump = confirmQuantityJump.value?.toDouble(),
            orderQtyJump = orderQuantityJump.value?.toDouble(),
            createdBy = _sessionData.value?.userName,
            updatedBy = _sessionData.value?.userName,
            imageLink = null
        )

        _isProgressBarActive.value = true
        coroutineScope.launch {
            val createItemDataDeferred = ItemApi.retrofitService.createItemForGroup(requestBody)
            try {
                createItemDataDeferred.await()
                _toastText.value = "Successfully created a new item"
            } catch (t:Throwable){
                _toastText.value = "Oops something went wrong, please try again"
            }
            _isProgressBarActive.value = false
        }

        // If successful, go back to products page
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}