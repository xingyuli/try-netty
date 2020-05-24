package chapter1

import java.util.*

class Movie(var title: String, var priceCode: Int) {
    companion object {
        const val CHILDRENS = 2
        const val REGULAR = 0
        const val NEW_RELEASE = 1
    }
}

class Rental(val movie: Movie, val daysRented: Int)

class Customer(val name: String) {
    private val _rentals = Vector<Rental>()

    fun addRental(rental: Rental) {
        _rentals.add(rental)
    }

    fun statement(): String {
        var totalAmount = 0.0
        var frequentRenterPoints = 0
        val rentals = _rentals.elements()
        var result = "Rental Record for $name\n"
        while (rentals.hasMoreElements()) {
            var thisAmount = 0.0
            val each = rentals.nextElement()

            // determine amounts for each line
            when (each.movie.priceCode) {
                Movie.REGULAR -> {
                    thisAmount += 2
                    if (each.daysRented > 2) {
                        thisAmount += (each.daysRented - 2) * 1.5
                    }
                }
                Movie.NEW_RELEASE -> {
                    thisAmount += each.daysRented * 3
                }
                Movie.CHILDRENS -> {
                    thisAmount += 1.5
                    if (each.daysRented > 3) {
                        thisAmount += (each.daysRented - 3) * 1.5
                    }
                }
            }

            // add frequent renter points
            frequentRenterPoints++
            // add bonus for a two day new release rental
            if (each.movie.priceCode == Movie.NEW_RELEASE && each.daysRented > 1) {
                frequentRenterPoints++
            }

            // show figures for this rental
            result += "\t${each.movie.title}\t${thisAmount}\n"
            totalAmount += thisAmount
        }

        result += "Amount owed is ${totalAmount}\n"
        result += "You earned $frequentRenterPoints frequent renter points"

        return result
    }

}