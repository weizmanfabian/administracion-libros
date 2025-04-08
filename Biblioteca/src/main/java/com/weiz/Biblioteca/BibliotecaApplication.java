package com.weiz.Biblioteca;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibliotecaApplication {

	public static void main(String[] args) {
		// Cargar el archivo .env
		Dotenv dotenv = Dotenv.configure()
				.directory(System.getProperty("user.dir"))
				.ignoreIfMissing()
				.load();

		// Establecer las variables como propiedades del sistema
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));

		// Iniciar la aplicación Spring Boot
		SpringApplication.run(BibliotecaApplication.class, args);
	}

}
