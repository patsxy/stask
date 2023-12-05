package com.sy.cc.api;

import java.util.List;


import com.github.pagehelper.PageInfo;
import com.sy.cc.entity.PageParams;
import com.sy.cc.entity.dto.ZySysJobDTO;
import com.sy.cc.util.ZyResponse;
import io.swagger.v3.oas.annotations.Operation;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;


/**
 * 任务Controller
 *
 * @author sy
 * @date 2023-11-03
 */
@Tag(name = "任务接口", description = "用于任务接口的定义")
@RequestMapping("/data/job")
public interface ZySysJobApi {


    /**
     * 查询任务列表
     */
    @Operation(summary = "查询任务列表")
    @RequestMapping(value = {"/getOne"})
    public ZyResponse<ZySysJobDTO> getOne(Long id);


    /**
     * 查询任务树列表
     */
    @Operation(summary = "查询任务树列表")
    @RequestMapping(value = {"/list"})
    public ZyResponse<List<ZySysJobDTO>> list(ZySysJobDTO zySysJobDTO);


    /**
     * 查询任务树列表
     */
    @Operation(summary = "查询任务树列表")
    @RequestMapping(value = {"/page"})
    public ZyResponse<PageInfo<ZySysJobDTO>> page(ZySysJobDTO zySysJobDTO, PageParams pageParams);

    /**
     * 新增任务
     */
    @Operation(summary = "新增任务")
    @RequestMapping("/add")
    public ZyResponse<Void> addSave(@RequestBody ZySysJobDTO zySysJobDTO);


    /**
     * 修改保存任务
     */
    @Operation(summary = "修改保存任务")
    @RequestMapping("/edit")
    public ZyResponse<Void> update(@RequestBody ZySysJobDTO zySysJobDTO);


    /**
     * 删除任务
     */
    @Operation(summary = "删除任务")
    @RequestMapping("/remove/{id}")
    public ZyResponse<Void> remove(@PathVariable("id") Long id);


    /**
     * 删除任务
     */
    @Operation(summary = "删除任务")
    @RequestMapping("/deleAll")
    public ZyResponse<Void> deleAll(@PathVariable("id") Long[] ids);


}
