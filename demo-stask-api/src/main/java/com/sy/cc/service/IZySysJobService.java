package com.sy.cc.service;
import com.github.pagehelper.PageInfo;
import com.sy.cc.entity.PageParams;
import com.sy.cc.comm.entity.ZySysJobDO;
import com.sy.cc.entity.dto.ZySysJobDTO;

import java.util.List;

public interface IZySysJobService {

    void insert(ZySysJobDO zySysJobDO);

    void updateById(ZySysJobDO zySysJobDO);


    void  deleteById(long id);
    ZySysJobDTO findById(long id);


    List<ZySysJobDO> selectAll();

    List<ZySysJobDTO> selectList(ZySysJobDTO zySysJobDTO);

    PageInfo<ZySysJobDTO> selectPage(ZySysJobDTO zySysJobDTO , PageParams pageParams) ;



}
