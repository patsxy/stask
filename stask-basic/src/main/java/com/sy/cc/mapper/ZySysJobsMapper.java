package com.sy.cc.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.sy.cc.comm.entity.ZySysJobDO;
import com.sy.cc.entity.dto.ZySysJobDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZySysJobsMapper extends BaseMapper<ZySysJobDO> {

   List<ZySysJobDTO> selectZySysJobList(ZySysJobDTO zySysJobDO);



}
