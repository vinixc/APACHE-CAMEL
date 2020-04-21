package br.com.vini.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;

import com.thoughtworks.xstream.XStream;

import br.com.vini.camel.model.Negociacao;

public class RotaPedidos2HttpPolling {
	//http://localhost:8080/webservices/ebook/item
	public static void main(String[] args) throws Exception {

		CamelContext context = new DefaultCamelContext();
		
		
		context.addRoutes(new RouteBuilder() {

			@Override
			public void configure() throws Exception {
				final XStream xstream = new XStream();
				xstream.alias("negociacao", Negociacao.class);
				
				from("timer://negociacoes?fixedRate=true&delay=1s&period=360s")
				.to("http4://argentumws-spring.herokuapp.com/negociacoes")
				.convertBodyTo(String.class)
				.unmarshal(new XStreamDataFormat(xstream))
				.split(body())
				.log("${body}")
//				.setHeader(Exchange.FILE_NAME, constant("negociacoes.xml"))
//				.to("file:saida");
				.end();
				
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}	
}
