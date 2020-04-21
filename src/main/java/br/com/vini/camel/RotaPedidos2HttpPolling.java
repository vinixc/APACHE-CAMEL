package br.com.vini.camel;

import java.sql.SQLException;
import java.text.SimpleDateFormat;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.dataformat.xstream.XStreamDataFormat;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import com.thoughtworks.xstream.XStream;

import br.com.vini.camel.model.Negociacao;

public class RotaPedidos2HttpPolling {
	//http://localhost:8080/webservices/ebook/item
	public static void main(String[] args) throws Exception {
		
		
		SimpleRegistry registro = new SimpleRegistry();
		registro.put("mysql", criaDataSource());
		
		CamelContext context = new DefaultCamelContext(registro);
		
		
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
				
				.process(new Processor() {
					
					@Override
					public void process(Exchange exchange) throws Exception {
						Negociacao negociacao = exchange.getIn().getBody(Negociacao.class);
			            exchange.setProperty("preco", negociacao.getPreco());
			            exchange.setProperty("quantidade", negociacao.getQuantidade());
			            String data = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss").format(negociacao.getData().getTime());
			            exchange.setProperty("data", data);
						
					}
				})
				
				.setBody(simple("insert into negociacao(preco, quantidade, data) values (${property.preco}, ${property.quantidade}, '${property.data}')"))
				
				.log("${body}")
				.delay(1000)
				.to("jdbc:mysql");
//				.setHeader(Exchange.FILE_NAME, constant("negociacoes.xml"))
//				.to("file:saida");
//				.end();
				
			}
		});
		
		context.start();
		Thread.sleep(20000);
		context.stop();
	}
	
	private static MysqlConnectionPoolDataSource criaDataSource() throws SQLException {
	    MysqlConnectionPoolDataSource mysqlDs = new MysqlConnectionPoolDataSource();
	    mysqlDs.setDatabaseName("camel");
	    mysqlDs.setServerName("localhost");
	    mysqlDs.setPort(3306);
	    mysqlDs.setUser("vinicius");
	    mysqlDs.setPassword("Vini123456789");
	    mysqlDs.setServerTimezone("UTC");
	    return mysqlDs;
	}
}
