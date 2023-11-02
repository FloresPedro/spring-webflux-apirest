package com.springboot.webflux.app;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;

import com.springboot.webflux.app.models.documents.Categoria;
import com.springboot.webflux.app.models.documents.Producto;
import com.springboot.webflux.app.models.services.IProductoService;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SpringBootWebfluxApirestApplication implements CommandLineRunner{
	
	@Autowired
	private IProductoService service;

	@Autowired
	private ReactiveMongoTemplate mongoTemplate;

	private static final Logger log = LoggerFactory.getLogger(SpringBootWebfluxApirestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootWebfluxApirestApplication.class, args);
	}
	
	@Override
	public void run(String... args) throws Exception {

		// Borrar los productos insertados cada vez que se inicie la aplicacion
		mongoTemplate.dropCollection("productos").subscribe();
		mongoTemplate.dropCollection("categorias").subscribe();

		Categoria electronico = new Categoria("Electronico");
		Categoria deportes = new Categoria("Deportes");
		Categoria computacion = new Categoria("Computacion");
		Categoria muebles = new Categoria("Muebles");

		Flux.just(electronico, deportes, computacion, muebles).flatMap(service::saveCategoria).doOnNext(c -> {
			log.info("Categoria creada: " + c.getNombre() + ", id: " + c.getId());
		}).thenMany(Flux.just(new Producto("TV Panasonic Pantalla LCD", 456.3, electronico),//para incluir otro flujo del tipo flux y no de tipo mono
				new Producto("Sony Camara HD Digital", 150.2, electronico),
				new Producto("Apple iPod", 49.8, electronico), new Producto("Sony NoteBook", 846.49, computacion),
				new Producto("Hewlett Packard Multifuncional", 200.2, computacion),
				new Producto("Bianchi Bicicleta", 70.50, deportes),
				new Producto("HP NoteBook Omen 17", 2500.48, computacion),
				new Producto("Mica Comoda 5 Cajones ", 70.56, muebles),
				new Producto("TV Sony Bravia OLED 4K Ultra HD", 3550.20, electronico)).flatMap(producto -> {
					producto.setCreateAt(new Date());
					return service.save(producto);
				})).subscribe(producto -> log.info("Insert: " + producto.getId() + " " + producto.getNombre()));

	}

}
