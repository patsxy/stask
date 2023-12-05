package com.sy.cc.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.sy.cc.entity.PageParams;
import com.sy.cc.comm.entity.ZySysJobDO;
import com.sy.cc.entity.dto.ZySysJobDTO;
import com.sy.cc.mapper.ZySysJobsMapper;
import com.sy.cc.service.IZySysJobService;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.List;

@Service
public class ZySysJobServiceImpl implements IZySysJobService {
    @Autowired
    private ZySysJobsMapper zySysJobsMapper;


    @Override
    public void insert(ZySysJobDO zySysJobDO) {
        zySysJobsMapper.insert(zySysJobDO);
    }

    @Override
    public void updateById(ZySysJobDO zySysJobDO) {
        zySysJobsMapper.updateById(zySysJobDO);
    }


    @Override
    public void deleteById(long id) {
        zySysJobsMapper.deleteById(id);
    }

    @Override
    public ZySysJobDTO findById(long id) {
        ZySysJobDTO zySysJobDTO = new ZySysJobDTO();
        ZySysJobDO zySysJobDO = zySysJobsMapper.selectById(id);
        if(zySysJobDO!=null) {
            BeanUtils.copyProperties(zySysJobDO, zySysJobDTO);
        }
        return zySysJobDTO;
    }

    @Override
    public List<ZySysJobDO> selectAll() {
        return zySysJobsMapper.selectList(null);
    }

    @Override
    public List<ZySysJobDTO> selectList(ZySysJobDTO zySysJobDTO) {
        List<ZySysJobDTO> zySysJobDOS = zySysJobsMapper.selectZySysJobList(zySysJobDTO);
        return zySysJobDOS;

    }

    @Override
    public PageInfo<ZySysJobDTO> selectPage(ZySysJobDTO zySysJobDTO, PageParams pageParams) {
        PageHelper.startPage(pageParams.getPageNum(), pageParams.getPageSize());
        List<ZySysJobDTO> zySysJobDOPage = zySysJobsMapper.selectZySysJobList(zySysJobDTO);
        PageInfo<ZySysJobDTO> zySysJobDTOPageInfo = new PageInfo<>(zySysJobDOPage);

        return zySysJobDTOPageInfo;
    }
}
