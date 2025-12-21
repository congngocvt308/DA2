package com.example.myapplication.utils

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * PremiumManager - Quản lý trạng thái Premium (thanh toán giả)
 * 
 * Sử dụng SharedPreferences để lưu trữ trạng thái đã mua.
 * Khi người dùng "thanh toán" thành công, trạng thái premium sẽ được lưu vĩnh viễn.
 * 
 * LƯU Ý: Đây là hệ thống thanh toán GIẢ dành cho testing.
 * Trong production, hãy thay thế bằng Google Play Billing Library.
 */
class PremiumManager private constructor(context: Context) {
    
    companion object {
        private const val PREF_NAME = "premium_preferences"
        private const val KEY_IS_PREMIUM = "is_premium_user"
        private const val KEY_PURCHASE_DATE = "purchase_date"
        private const val KEY_PURCHASE_TOKEN = "purchase_token"
        
        @Volatile
        private var INSTANCE: PremiumManager? = null
        
        fun getInstance(context: Context): PremiumManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: PremiumManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    
    private val _isPremium = MutableStateFlow(prefs.getBoolean(KEY_IS_PREMIUM, false))
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()
    
    /**
     * Kiểm tra xem người dùng đã mua premium chưa
     */
    fun checkPremiumStatus(): Boolean {
        return prefs.getBoolean(KEY_IS_PREMIUM, false)
    }
    
    /**
     * Xử lý thanh toán giả (fake purchase)
     * Trong thực tế, đây sẽ là callback từ Google Play Billing
     * 
     * @return true nếu "thanh toán" thành công
     */
    fun processFakePurchase(): Boolean {
        return try {
            // Giả lập việc xử lý thanh toán
            val fakeToken = "FAKE_PURCHASE_TOKEN_${System.currentTimeMillis()}"
            
            prefs.edit().apply {
                putBoolean(KEY_IS_PREMIUM, true)
                putLong(KEY_PURCHASE_DATE, System.currentTimeMillis())
                putString(KEY_PURCHASE_TOKEN, fakeToken)
                apply()
            }
            
            _isPremium.value = true
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Khôi phục giao dịch mua (restore purchase)
     * Trong hệ thống giả này, chỉ đơn giản là kiểm tra SharedPreferences
     */
    fun restorePurchase(): Boolean {
        val isPurchased = prefs.getBoolean(KEY_IS_PREMIUM, false)
        _isPremium.value = isPurchased
        return isPurchased
    }
    
    /**
     * Reset trạng thái premium (chỉ dùng cho testing)
     */
    fun resetPremiumForTesting() {
        prefs.edit().apply {
            putBoolean(KEY_IS_PREMIUM, false)
            remove(KEY_PURCHASE_DATE)
            remove(KEY_PURCHASE_TOKEN)
            apply()
        }
        _isPremium.value = false
    }
    
    /**
     * Lấy ngày mua premium
     */
    fun getPurchaseDate(): Long? {
        val date = prefs.getLong(KEY_PURCHASE_DATE, -1)
        return if (date > 0) date else null
    }
}

