package com.example.plantlets.models

import android.os.Parcelable
import com.example.plantlets.utils.Constants.Non_ACTIVE
import com.example.plantlets.utils.Extensions.addDays
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import kotlinx.android.parcel.Parcelize
import java.util.Date
@Parcelize
data class Store(
    var email:String?=null,
    var storeName:String = "",
    var storeAddress:String = "",
    var storePinLocation:String="",
    val freeTrialDate: Timestamp = Timestamp.now(),
    var freeTrialExpireDate:Timestamp = Timestamp(Date().addDays(30)) ,
    var activeStatus:Boolean = false,
    var location: Location?=null,
    var displayImage: String?=null,
    var bannerImage:String?=null,
    var status:String=Non_ACTIVE,
    var contact:String?=null,
    var totalRating:Double=5.0,
    var totalOrders:Int=0,
    var image:String?=null,
):Parcelable
