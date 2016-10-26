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
}
