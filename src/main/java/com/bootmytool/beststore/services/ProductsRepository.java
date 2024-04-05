package com.bootmytool.beststore.services;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bootmytool.beststore.models.product;

public interface ProductsRepository extends JpaRepository<product, Integer>{

}
