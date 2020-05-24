package chapter1

import java.lang.IllegalArgumentException

class Movie private constructor(var title: String, private var price: Price) {

    constructor(title: String, priceCode: Int) : this(title, Price.fromCode(priceCode))

    var priceCode: Int
        get() = price.priceCode
        set(value) {
            price = Price.fromCode(value)
        }

    fun getCharge(daysRented: Int): Double {
        return price.getCharge(daysRented)
    }

    fun getFrequentRenterPoints(daysRented: Int): Int {
        return price.getFrequentRenterPoints(daysRented)
    }

    private sealed class Price(val priceCode: Int) {

        abstract fun getCharge(daysRented: Int): Double

        class ChildrensPrice : Price(CHILDRENS) {

            override fun getCharge(daysRented: Int): Double {
                var result = 1.5
                if (daysRented > 3) {
                    result += (daysRented - 3) * 1.5
                }
                return result
            }

        }

        class NewReleasePrice : Price(NEW_RELEASE) {

            override fun getCharge(daysRented: Int): Double {
                return daysRented * 3.0
            }

            override fun getFrequentRenterPoints(daysRented: Int): Int {
                return if (daysRented > 1) 2 else 1
            }

        }

        class RegularPrice : Price(REGULAR) {

            override fun getCharge(daysRented: Int): Double {
                var result = 2.0
                if (daysRented > 2) {
                    result += (daysRented - 2) * 1.5
                }
                return result
            }

        }

        open fun getFrequentRenterPoints(daysRented: Int): Int = 1

        companion object {

            fun fromCode(code: Int): Price = when (code) {
                REGULAR -> {
                    RegularPrice()
                }
                CHILDRENS -> {
                    ChildrensPrice()
                }
                NEW_RELEASE -> {
                    NewReleasePrice()
                }
                else -> throw IllegalArgumentException("Incorrect Price Code")
            }

        }

    }

    companion object {
        const val CHILDRENS = 2
        const val REGULAR = 0
        const val NEW_RELEASE = 1
    }

}