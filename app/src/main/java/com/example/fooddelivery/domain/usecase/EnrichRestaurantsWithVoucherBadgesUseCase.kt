package com.example.fooddelivery.domain.usecase

import com.example.fooddelivery.domain.config.VoucherFeatureFlags
import com.example.fooddelivery.domain.model.Restaurant
import com.example.fooddelivery.domain.repository.VoucherRepository
import com.example.fooddelivery.domain.util.pickBestBadgeLabel
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

@Singleton
class EnrichRestaurantsWithVoucherBadgesUseCase @Inject constructor(
    private val voucherRepository: VoucherRepository
) {
    private val cache = mutableMapOf<Int, String?>()

    suspend operator fun invoke(
        restaurants: List<Restaurant>,
        onlyIfHasVoucher: Boolean = false
    ): List<Restaurant> {
        if (!VoucherFeatureFlags.RESTAURANT_PUBLIC_VOUCHERS_ENABLED) {
            return restaurants
        }
        return enrichWithVoucherBadges(restaurants, onlyIfHasVoucher)
    }

    private suspend fun enrichWithVoucherBadges(
        restaurants: List<Restaurant>,
        onlyIfHasVoucher: Boolean
    ): List<Restaurant> = coroutineScope {
        val semaphore = Semaphore(MAX_CONCURRENT_REQUESTS)
        restaurants.map { restaurant ->
            async {
                enrichRestaurant(restaurant, onlyIfHasVoucher, semaphore)
            }
        }.awaitAll()
    }

    private suspend fun enrichRestaurant(
        restaurant: Restaurant,
        onlyIfHasVoucher: Boolean,
        semaphore: Semaphore
    ): Restaurant {
        if (onlyIfHasVoucher && !restaurant.hasVoucher) {
            return restaurant
        }

        val restaurantId = restaurant.id.toIntOrNull() ?: return restaurant
        val label = cache.getOrPut(restaurantId) {
            semaphore.withPermit { fetchBadgeLabel(restaurantId) }
        }

        return if (label != null) {
            restaurant.copy(
                hasVoucher = true,
                voucherBadgeLabel = label
            )
        } else {
            restaurant.copy(
                hasVoucher = false,
                voucherBadgeLabel = null
            )
        }
    }

    private suspend fun fetchBadgeLabel(restaurantId: Int): String? {
        return voucherRepository.getCustomerVouchers(restaurantId)
            .getOrNull()
            ?.pickBestBadgeLabel()
    }

    companion object {
        private const val MAX_CONCURRENT_REQUESTS = 5
    }
}
