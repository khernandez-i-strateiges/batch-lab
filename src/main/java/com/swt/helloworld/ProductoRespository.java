package com.swt.helloworld;

import com.swt.helloworld.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRespository extends JpaRepository<Producto, Long> {
}
