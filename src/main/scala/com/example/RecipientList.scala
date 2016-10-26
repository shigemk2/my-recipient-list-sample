package com.example

import akka.actor._
import com.example._

case class RequestForQuotation(rfqId: String, retailItems: Seq[RetailItem]) {
  val totalRetailPrice: Double = retailItems.map(retailItem => retailItem.retailPrice).sum
}

case class RetailItem(itemId: String, retailPrice: Double)

case class PriceQuoteInterest(path: String, quoteProcessor: ActorRef, lowTotalRetail: Double, highTotalRetail: Double)

case class RequestPriceQuote(rfqId: String, itemId: String, retailPrice: Double, orderTotalRetailPrice: Double)

case class PriceQuote(rfqId: String, itemId: String, retailPrice: Double, discountPrice: Double)

object RecipientListDriver {
  val orderProcessor = system.actorOf(Props[MountaineeringSuppliesOrderProcessor], "orderProcessor")

  system.actorOf(Props(classOf[BudgetHikersPriceQuotes], orderProcessor), "budgetHikers")
  system.actorOf(Props(classOf[HighSierraPriceQuotes], orderProcessor), "highSierra")
  system.actorOf(Props(classOf[MountainAscentPriceQuotes], orderProcessor), "mountainAscent")
  system.actorOf(Props(classOf[PinnacleGearPriceQuotes], orderProcessor), "pinnacleGear")
  system.actorOf(Props(classOf[RockBottomOuterwearPriceQuotes], orderProcessor), "rockBottomOuterwear")

  orderProcessor ! RequestForQuotation("123",
    Vector(RetailItem("1", 29.95),
      RetailItem("2", 99.95),
      RetailItem("3", 14.95)))

  orderProcessor ! RequestForQuotation("125",
    Vector(RetailItem("4", 39.99),
      RetailItem("5", 199.95),
      RetailItem("6", 149.95),
      RetailItem("7", 724.99)))

  orderProcessor ! RequestForQuotation("129",
    Vector(RetailItem("8", 119.99),
      RetailItem("9", 499.95),
      RetailItem("10", 519.00),
      RetailItem("11", 209.50)))

  orderProcessor ! RequestForQuotation("135",
    Vector(RetailItem("12", 0.97),
      RetailItem("13", 9.50),
      RetailItem("14", 1.99)))

  orderProcessor ! RequestForQuotation("140",
    Vector(RetailItem("15", 107.50),
      RetailItem("16", 9.50),
      RetailItem("17", 599.99),
      RetailItem("18", 249.95),
      RetailItem("19", 789.99)))
}
