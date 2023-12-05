package com.sy.cc.controller;

import java.util.List;
import java.util.Objects;


import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;


import com.github.pagehelper.PageInfo;
import com.sy.cc.api.ZySysJobApi;
import com.sy.cc.entity.PageParams;
import com.sy.cc.comm.entity.ZySysJobDO;
import com.sy.cc.entity.dto.ZySysJobDTO;
import com.sy.cc.service.IZySysJobService;
import com.sy.cc.util.ZyResponse;
import com.sy.cc.util.ZyResponseUtil;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * 任务Controller
 *
 * @author sy
 * @date 2023-11-03
 */
@RestController
public class ZySysJobController implements ZySysJobApi {


    @Autowired
    private IZySysJobService zySysJobService;


    /**
     * 查询任务列表
     */
    @Override
    public ZyResponse<ZySysJobDTO> getOne(Long id) {

        ZySysJobDTO zySysJobDTO = zySysJobService.findById(id);
        return ZyResponseUtil.success(zySysJobDTO);
    }

    /**
     * 查询任务树列表
     */
    @Override
    public ZyResponse<List<ZySysJobDTO>> list(ZySysJobDTO zySysJobDTO) {
        List<ZySysJobDTO> list = zySysJobService.selectList(zySysJobDTO);
        return ZyResponseUtil.success(list);
    }

    @Override
    public ZyResponse<PageInfo<ZySysJobDTO>> page(ZySysJobDTO zySysJobDTO, PageParams pageParams) {

        PageInfo<ZySysJobDTO> page = zySysJobService.selectPage(zySysJobDTO, pageParams);
        return ZyResponseUtil.success(page);
    }

    /**
     * 新增任务
     */
    @Override
    public ZyResponse<Void> addSave(ZySysJobDTO zySysJobDTO) {
        if (Objects.nonNull(zySysJobDTO)) {
            ZySysJobDO zySysJobDO = new ZySysJobDO();
            BeanUtils.copyProperties(zySysJobDTO, zySysJobDO);
            zySysJobService.insert(zySysJobDO);
        } else {
            throw new NullPointerException();
        }
        return ZyResponseUtil.success();
    }


    /**
     * 修改任务
     */

    @Override
    public ZyResponse<Void> update(ZySysJobDTO zySysJobDTO) {

        if (Objects.nonNull(zySysJobDTO)) {
            ZySysJobDO zySysJobDO = new ZySysJobDO();
            BeanUtils.copyProperties(zySysJobDTO, zySysJobDO);
            zySysJobService.updateById(zySysJobDO);
        } else {
            throw new NullPointerException();
        }

        return ZyResponseUtil.success();
    }


    /**
     * 删除任务
     */

    @Override
    public ZyResponse<Void> remove(Long id) {
        if (Objects.nonNull(id)) {

            zySysJobService.deleteById(id);
        } else {
            throw new NullPointerException();
        }

        return ZyResponseUtil.success();
    }

    /**
     * 删除任务
     */

    @Override
    public ZyResponse<Void> deleAll(Long[] ids) {
        if (ArrayUtils.isNotEmpty(ids)) {
            for (Long id : ids) {

                remove(id);

            }
        }
        return ZyResponseUtil.success();
    }
}
