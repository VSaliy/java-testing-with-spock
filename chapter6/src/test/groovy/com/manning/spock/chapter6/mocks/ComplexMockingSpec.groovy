package com.manning.spock.chapter6.mocks

import spock.lang.*

import com.manning.spock.chapter6.Basket
import com.manning.spock.chapter6.Product
import com.manning.spock.chapter6.stubs.WarehouseInventory

@Subject(BillableBasket.class)
class ComplexMockingSpec extends spock.lang.Specification{

	
	
	def "card has no funds"() {
		given: "an basket, a customer and a TV"
		Product tv = new Product(name:"bravia",price:1200,weight:18)
		Product camera = new Product(name:"panasonic",price:350,weight:2)
		BillableBasket basket = new BillableBasket()
		Customer customer = new Customer(name:"John",vip:false,creditCard:"testCard")

		and:"a credit card service"
		CreditCardProcessor creditCardSevice = Mock(CreditCardProcessor)
		basket.setCreditCardProcessor(creditCardSevice)
		
		and:"a warehouse"
		WarehouseInventory inventory = Stub(WarehouseInventory)
		{
			availableOfProduct(_ , _) >> false
			isEmpty() >> true
		}
		basket.setWarehouseInventory(inventory)

		when: "user checks out the tv"
		basket.addProduct tv
		basket.addProduct camera
		boolean charged = basket.fullCheckout(customer)

		then: "credit card is checked"
		1 * creditCardSevice.authorize(1550, customer) >>  CreditCardResult.NO_ENOUGH_FUNDS
		!charged
		0 * _
		
	}
	
	def "happy path for credit card sale"() {
		given: "an basket, a customer and a TV"
		Product tv = new Product(name:"bravia",price:1200,weight:18)
		Product camera = new Product(name:"panasonic",price:350,weight:2)
		BillableBasket basket = new BillableBasket()
		Customer customer = new Customer(name:"John",vip:false,creditCard:"testCard")

		and:"a credit card service"
		CreditCardProcessor creditCardSevice = Mock(CreditCardProcessor)
		basket.setCreditCardProcessor(creditCardSevice)
		
		and:"a warehouse"
		WarehouseInventory inventory = Mock(WarehouseInventory)
		basket.setWarehouseInventory(inventory)

		when: "user checks out the tv"
		basket.addProduct tv
		basket.addProduct camera
		boolean charged = basket.fullCheckout(customer)

		then: "credit card is checked"
		interaction {
			CreditCardResult sampleResult = CreditCardResult.OK
			sampleResult.setToken("sample");
			1 * creditCardSevice.authorize(1550, customer) >>  sampleResult
		}
		
		then: "inventory is checked"
		2 * inventory.availableOfProduct(!null , 1) >> true
		
		then: "credit card is charged"
		1 * creditCardSevice.capture({myToken -> myToken.endsWith("sample")}, customer) >>  CreditCardResult.OK
		charged
	}
	
	

	
}

