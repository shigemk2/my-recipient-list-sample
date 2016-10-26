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

object RecipientListDriver extends CompletableApp(5) {
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

class MountaineeringSuppliesOrderProcessor extends Actor {
  val interestRegistry = scala.collection.mutable.Map[String, PriceQuoteInterest]()

  def calculateRecipientList(rfq: RequestForQuotation): Iterable[ActorRef] = {
    for {
      interest <- interestRegistry.values
      if (rfq.totalRetailPrice >= interest.lowTotalRetail)
      if (rfq.totalRetailPrice <= interest.highTotalRetail)
    } yield interest.quoteProcessor
  }

  def dispatchTo(rfq: RequestForQuotation, recipientList: Iterable[ActorRef]) = {
    recipientList.foreach { recipient =>
      rfq.retailItems.foreach { retailItem =>
        println("OrderProcessor: " + rfq.rfqId + " item: " + retailItem.itemId + " to: " + recipient.path.toString)
        recipient ! RequestPriceQuote(rfq.rfqId, retailItem.itemId, retailItem.retailPrice, rfq.totalRetailPrice)
      }
    }
  }

  def receive = {
    case interest: PriceQuoteInterest =>
      interestRegistry(interest.path) = interest
    case priceQuote: PriceQuote =>
      println(s"OrderProcessor: received: $priceQuote")
    case rfq: RequestForQuotation =>
      val recipientList = calculateRecipientList(rfq)
      dispatchTo(rfq, recipientList)
    case message: Any =>
      println(s"OrderProcessor: received unexpected message: $message")
  }
}

class BudgetHikersPriceQuotes(interestRegistrar: ActorRef) extends Actor {
  interestRegistrar ! PriceQuoteInterest(self.path.toString, self, 1.00, 1000.00)

  def receive = {
    case rpq: RequestPriceQuote =>
      val discount = discountPercentage(rpq.orderTotalRetailPrice) * rpq.retailPrice
      sender ! PriceQuote(rpq.rfqId, rpq.itemId, rpq.retailPrice, rpq.retailPrice - discount)

    case message: Any =>
      println(s"BudgetHikersPriceQuotes: received unexpected message: $message")
  }

  def discountPercentage(orderTotalRetailPrice: Double) = {
    if (orderTotalRetailPrice <= 100.00) 0.02
    else if (orderTotalRetailPrice <= 399.99) 0.03
    else if (orderTotalRetailPrice <= 499.99) 0.05
    else if (orderTotalRetailPrice <= 799.99) 0.07
    else 0.075
  }
}

class HighSierraPriceQuotes(interestRegistrar: ActorRef) extends Actor {
  interestRegistrar ! PriceQuoteInterest(self.path.toString, self, 100.00, 10000.00)

  def receive = {
    case rpq: RequestPriceQuote =>
      val discount = discountPercentage(rpq.orderTotalRetailPrice) * rpq.retailPrice
      sender ! PriceQuote(rpq.rfqId, rpq.itemId, rpq.retailPrice, rpq.retailPrice - discount)

    case message: Any =>
      println(s"HighSierraPriceQuotes: received unexpected message: $message")
  }

  def discountPercentage(orderTotalRetailPrice: Double): Double = {
    if (orderTotalRetailPrice <= 150.00) 0.015
    else if (orderTotalRetailPrice <= 499.99) 0.02
    else if (orderTotalRetailPrice <= 999.99) 0.03
    else if (orderTotalRetailPrice <= 4999.99) 0.04
    else 0.05
  }
}

class MountainAscentPriceQuotes(interestRegistrar: ActorRef) extends Actor {
  interestRegistrar ! PriceQuoteInterest(self.path.toString, self, 70.00, 5000.00)

  def receive = {
    case rpq: RequestPriceQuote =>
      val discount = discountPercentage(rpq.orderTotalRetailPrice) * rpq.retailPrice
      sender ! PriceQuote(rpq.rfqId, rpq.itemId, rpq.retailPrice, rpq.retailPrice - discount)

    case message: Any =>
      println(s"MountainAscentPriceQuotes: received unexpected message: $message")
  }

  def discountPercentage(orderTotalRetailPrice: Double) = {
    if (orderTotalRetailPrice <= 99.99) 0.01
    else if (orderTotalRetailPrice <= 199.99) 0.02
    else if (orderTotalRetailPrice <= 499.99) 0.03
    else if (orderTotalRetailPrice <= 799.99) 0.04
    else if (orderTotalRetailPrice <= 999.99) 0.045
    else if (orderTotalRetailPrice <= 2999.99) 0.0475
    else 0.05
  }
}

class PinnacleGearPriceQuotes(interestRegistrar: ActorRef) extends Actor {
  interestRegistrar ! PriceQuoteInterest(self.path.toString, self, 250.00, 500000.00)

  def receive = {
    case rpq: RequestPriceQuote =>
      val discount = discountPercentage(rpq.orderTotalRetailPrice) * rpq.retailPrice
      sender ! PriceQuote(rpq.rfqId, rpq.itemId, rpq.retailPrice, rpq.retailPrice - discount)

    case message: Any =>
      println(s"PinnacleGearPriceQuotes: received unexpected message: $message")
  }

  def discountPercentage(orderTotalRetailPrice: Double) = {
    if (orderTotalRetailPrice <= 299.99) 0.015
    else if (orderTotalRetailPrice <= 399.99) 0.0175
    else if (orderTotalRetailPrice <= 499.99) 0.02
    else if (orderTotalRetailPrice <= 999.99) 0.03
    else if (orderTotalRetailPrice <= 1199.99) 0.035
    else if (orderTotalRetailPrice <= 4999.99) 0.04
    else if (orderTotalRetailPrice <= 7999.99) 0.05
    else 0.06
  }
}