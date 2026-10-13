package com.baozistore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baozistore.model.Cliente;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

}
