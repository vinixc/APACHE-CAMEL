package br.com.vini.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.impl.DefaultCamelContext;

public class RotaPedidos2HttpSubRotaSeda {
	//http://localhost:8080/webservices/ebook/item
	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				
				from("file:pedidos?delay=5s&noop=true")
				.routeId("rota-pedidos")
				.to("seda:soap")
				.to("seda:http");
				
				from("seda:http")
				.routeId("rota-http")
				.setProperty("pedidoId", xpath("/pedido/id/text()"))
				.setProperty("clienteId", xpath("/pedido/pagamento/email-titular/text()"))
				.split()
					.xpath("/pedido/itens/item")
				.filter()
					.xpath("/item/formato[text()='EBOOK']")
				.setProperty("ebookId", xpath("/item/livro/codigo/text()"))
				.log("${id}")
				.log("${exchange.pattern}")
				.marshal().xmljson()
				.log("${body}")
				.setHeader(Exchange.HTTP_METHOD, HttpMethods.GET)
				.setHeader(Exchange.HTTP_QUERY, simple("ebookId=${property.ebookId}&pedidoId=${property.pedidoId}&clienteId=${property.clienteId}"))
				.to("http4://localhost:8080/webservices/ebook/item");
				
				from("seda:soap")
				.routeId("rota-soap")
				.setBody(constant("<envelope>Teste</envelope>"))
				.log("${body}")
				.to("mock:soap");
				
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}	
}
