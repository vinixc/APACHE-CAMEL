package br.com.vini.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos2HttpPOST {
	//http://localhost:8080/webservices/ebook/item
	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
				from("file:pedidos?delay=5s&noop=true")
				.split()
					.xpath("/pedido/itens/item")
				.filter()
					.xpath("/item/formato[text()='EBOOK']")
				.log("${id}")
				.log("${exchange.pattern}")
				.marshal().xmljson()
				.log("${body}")
				.setHeader(Exchange.HTTP_METHOD, HttpMethods.POST)
				.to("http4://localhost:8080/webservices/ebook/item");
				
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}	
}
