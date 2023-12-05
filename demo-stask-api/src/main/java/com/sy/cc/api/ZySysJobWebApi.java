package com.sy.cc.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;


public interface ZySysJobWebApi {


    @RequestMapping( "/")
    public String index();

    @RequestMapping("/add")
    public String add();

    @RequestMapping("/edit/{id}")
    public ModelAndView edit(@PathVariable("id") Long id);

}
