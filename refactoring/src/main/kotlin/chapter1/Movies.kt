package chapter1

import java.util.*

class Movie(var title: String, var priceCode: Int) {
    companion object {
        const val CHILDRENS = 2
        const val REGULAR = 0
        const val NEW_RELEASE = 1
    }
}

class Rental(val movie: Movie, val daysRented: Int) {

    fun getCharge(): Double {
        var result = 0.0

        when (movie.priceCode) {
            Movie.REGULAR -> {
                result += 2
                if (daysRented > 2) {
                    result += (daysRented - 2) * 1.5
                }
            }
            Movie.NEW_RELEASE -> {
                result += daysRented * 3
            }
            Movie.CHILDRENS -> {
                result += 1.5
                if (daysRented > 3) {
                    result += (daysRented - 3) * 1.5
                }
            }
        }

        return result
    }

    fun getFrequentRenterPoints(): Int {
        return if (movie.priceCode == Movie.NEW_RELEASE && daysRented > 1) {
            2
        } else {
            1
        }
    }

}

class Customer(val name: String) {
    private val _rentals = Vector<Rental>()

    fun addRental(rental: Rental) {
        _rentals.add(rental)
    }

    fun statement(): String {
        val rentals = _rentals.elements()

        var result = "Rental Record for $name\n"
        while (rentals.hasMoreElements()) {
            val each = rentals.nextElement()

            // show figures for this rental
            result += "\t${each.movie.title}\t${each.getCharge()}\n"
        }

        // add footer lines
        result += "Amount owed is ${getTotalCharge()}\n"
        result += "You earned ${getTotalFrequentRenterPoints()} frequent renter points"

        return result
    }

    fun htmlStatement(): String {
        val rentals = _rentals.elements()

        var result = "<H1>Rentals for <EM>${name}</EM</H1><P>\n"
        while (rentals.hasMoreElements()) {
            val each = rentals.nextElement()

            // show figures for each rental
            result += "${each.movie.title}: ${each.getCharge()}<BR>\n"
        }

        // add footer lines
        result += "<P>You owe <EM>${getTotalCharge()}</EM><P>\n"
        result += "On this rental you earned <EM>${getTotalFrequentRenterPoints()}</EM> frequent renter points<P>"

        return result
    }

    private fun getTotalCharge(): Double {
        var result = 0.0

        val rentals = _rentals.elements()
        while (rentals.hasMoreElements()) {
            val each = rentals.nextElement()
            result += each.getCharge()
        }

        return result
    }

    private fun getTotalFrequentRenterPoints(): Int {
        var result = 0

        val rentals = _rentals.elements()
        while (rentals.hasMoreElements()) {
            val each = rentals.nextElement()
            result += each.getFrequentRenterPoints()
        }

        return result
    }

}