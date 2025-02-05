package com.example.plantlets.fragments.user

import android.content.SharedPreferences
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.plantlets.Manager.CartManager
import com.example.plantlets.R
import com.example.plantlets.activities.UserHomeActivity
import com.example.plantlets.databinding.FragmentCheckoutBinding
import com.example.plantlets.fragments.seller.AddItemFragmentArgs
import com.example.plantlets.models.Amounts
import com.example.plantlets.models.CartItem
import com.example.plantlets.models.DeliveryInfo
import com.example.plantlets.models.Order
import com.example.plantlets.models.SellerItem
import com.example.plantlets.models.User
import com.example.plantlets.utils.Constants
import com.example.plantlets.utils.Constants.CART
import com.example.plantlets.utils.Constants.CASH_ON_DELIVERY
import com.example.plantlets.utils.Constants.LATITUDE
import com.example.plantlets.utils.Constants.LOCATION_DATA
import com.example.plantlets.utils.Constants.LONGITUDE
import com.example.plantlets.utils.Constants.ORDER_PENDING
import com.example.plantlets.utils.Constants.ORDER_PLACED
import com.example.plantlets.utils.Helper.generateRandomStringWithTime
import com.example.plantlets.utils.Helper.getAddressFromLocation
import com.example.plantlets.utils.Helper.getCurrentDateFormatted
import com.example.plantlets.viewmodels.user.CheckoutViewModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Dispatcher
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private lateinit var binding: FragmentCheckoutBinding
    private lateinit var checkoutViewModel: CheckoutViewModel
    private var amount: Amounts? = null

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    @Inject
    lateinit var cartManager: CartManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        checkoutViewModel = ViewModelProvider(this).get(CheckoutViewModel::class.java)
        (requireActivity() as UserHomeActivity).apply {
            changeIconCartFill()
        }
        init()
        handleLocationResult()
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as UserHomeActivity).showBottomNav()

    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as UserHomeActivity).apply {
            changeIconCart()
        }
    }

    private fun init() {
        val args: CheckoutFragmentArgs by navArgs()
        amount = args.amounts
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            user = checkoutViewModel.getUser()
            deliveryInfo = DeliveryInfo()
        }

        with(binding) {
            rlPinLocation.setOnClickListener {
                if (findNavController().currentDestination?.id == R.id.checkoutFragment) {
                    val action =
                        CheckoutFragmentDirections.actionCheckoutFragmentToPinLocationFragment()
                    findNavController().navigate(action)
                }
            }
            tvPinLocation.setOnClickListener {
                if (findNavController().currentDestination?.id == R.id.checkoutFragment) {
                    val action =
                        CheckoutFragmentDirections.actionCheckoutFragmentToPinLocationFragment()
                    findNavController().navigate(action)
                }
            }
            btnCheckout.setOnClickListener {
                moveToOrderDetails()
            }
        }

    }

    fun moveToOrderDetails() {
        val order = Order(
            customerInfo = binding.user ?: User(),
            customerDeliveryInfo = binding.deliveryInfo ?: DeliveryInfo(),
            cartItemList = getCartItemList(),
            paymentMethod = CASH_ON_DELIVERY,
            amounts = amount ?: Amounts(),
            orderStatus = ORDER_PENDING,
            storeId = cartManager.store?.email!!,
            date = getCurrentDateFormatted(),
            gardenerService = getRadioButtonValue()
        )
        CoroutineScope(Dispatchers.Main).launch {
            order.orderId = checkoutViewModel.getOrderId()
            checkoutViewModel.placeOrder(order)
            withContext(Dispatchers.Main) {
                if (findNavController().currentDestination?.id == R.id.checkoutFragment) {
                    val action =
                        CheckoutFragmentDirections.actionCheckoutFragmentToOrderDetailsFragment(
                            order
                        )
                    findNavController().navigate(action)
                }
            }

        }


    }

    fun getRadioButtonValue(): Boolean {
        binding.apply {
            when (binding.rgGardener.checkedRadioButtonId) {
                R.id.rb_yes -> return true
                R.id.rb_no -> return false
            }
        }
        return false
    }

    private fun placeOrder() {

        val order = Order(
            orderId = generateRandomStringWithTime(),
            customerInfo = binding.user ?: User(),
            customerDeliveryInfo = binding.deliveryInfo ?: DeliveryInfo(),
            cartItemList = getCartItemList(),
            paymentMethod = CASH_ON_DELIVERY,
            amounts = amount ?: Amounts(),
            orderStatus = ORDER_PENDING,
            storeId = cartManager.store?.email!!
        )
        checkoutViewModel.placeOrder(order)
    }

    private fun getCartItemList(): List<CartItem> {
        val cartJson = sharedPreferences.getString(CART, null)
        return if (!cartJson.isNullOrEmpty()) {
            val type = object : TypeToken<MutableList<CartItem>?>() {}.type
            Gson().fromJson<MutableList<CartItem>>(cartJson, type)
        } else {
            emptyList()
        }
    }

    private fun handleLocationResult() {
        val navBackStackEntry = findNavController().getBackStackEntry(R.id.checkoutFragment)
        val locationData = navBackStackEntry.savedStateHandle.getLiveData<Bundle>(LOCATION_DATA)
        locationData.observe(viewLifecycleOwner) { data ->
            val latitude = data.getDouble(LATITUDE)
            val longitude = data.getDouble(LONGITUDE)

            setAddressFromLocation(latitude, longitude)
        }
    }

    private fun setAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(requireContext(), Locale.getDefault())

//        binding.deliveryInfo = DeliveryInfo(
//            fullAddress = binding.deliveryInfo?.fullAddress,
//            pinAddress = getAddressFromLocation(geocoder, latitude, longitude),
//            locationLatitude = latitude,
//            locationLongitude = longitude
//        )
        binding.deliveryInfo?.pinAddress = getAddressFromLocation(geocoder, latitude, longitude)
        binding.deliveryInfo?.locationLatitude = latitude
        binding.deliveryInfo?.locationLongitude = longitude
        binding.deliveryInfo = binding.deliveryInfo
        binding.invalidateAll()
        Log.d("USMAN-TAG", binding.deliveryInfo?.pinAddress ?: "no address")
    }

}