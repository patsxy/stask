package com.sy.cc.controller;

import com.sy.cc.api.ZySysJobWebApi;

import com.sy.cc.entity.dto.ZySysJobDTO;
import com.sy.cc.comm.exception.ZyException;
import com.sy.cc.service.IZySysJobService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.ModelAndView;



@Controller
public class ZySysJobWebContorller implements ZySysJobWebApi {
    @Value("${server.port:8080}")
    private String port;

    @Autowired
    private IZySysJobService sysJobService;

    @Override
    public String index() {

        return "job.html";
    }

    @Override
    public String add() {
        return "add.html";
    }

    @Override
    public ModelAndView edit(Long id) {
        ZySysJobDTO byId = sysJobService.findById(id);
        ModelAndView modelAndView = new ModelAndView("edit.html");


        if (byId != null) {
            modelAndView.getModel().put("zySysJob", byId);
        } else {
            throw new ZyException("id未找到！");
        }
        return modelAndView;
    }
}
