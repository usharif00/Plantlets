package com.example.plantlets.viewmodels

import androidx.lifecycle.ViewModel
import com.example.plantlets.Response.CustomResponse
import com.example.plantlets.models.Order
import com.example.plantlets.models.Store
import com.example.plantlets.models.User
import com.example.plantlets.repositories.LocalRepository
import com.example.plantlets.repositories.OrderRepository
import com.example.plantlets.utils.Constants.STORE_REFRENCE_USER
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject


@HiltViewModel
class OrdersViewModel @Inject constructor(
    private val localRepository: LocalRepository,
    private val orderRepository: OrderRepository, )
    : ViewModel() {

    val ordersList: StateFlow<CustomResponse<List<Order>>>
        get() = orderRepository.ordersStateFlow

    var query: String? = null

    fun startObserving() {
        localRepository.getStoreFromPref()?.let {
            orderRepository.getStoreOrders(it.email)
        }
    }

    fun stopObserving() {
        orderRepository.removeListener()
    }

    fun getOrdersBySearch(query: String): List<Order> {

        val filteredItems = ordersList.value?.data?.filter { order ->
            order.orderId.contains(query) ||
                    order.orderStatus.contains(query) ||
                    order.date.contains(query) ||
                    order.customerInfo?.name?.contains(query)==true ||
                    order.amounts?.totalAmount.toString().contains(query)
        } ?: emptyList()
        return filteredItems

    }

//    fun setStore(store:Store?){
//        localRepository.saveStoreDataToSharedPreferences(store,STORE_REFRENCE_USER)
//    }
//    fun getStore():Store?{
//        return localRepository.getStoreFromPref(STORE_REFRENCE_USER)
//    }
//
//    fun getUserData(): User? {
//        return localRepository.getCurrentUserData()
//    }


}