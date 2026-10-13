package com.baozistore.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.baozistore.model.Produto;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

}
