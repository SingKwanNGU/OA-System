package me.SingKwan.process.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import me.SingKwan.model.process.Process;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import me.SingKwan.vo.process.ProcessQueryVo;
import me.SingKwan.vo.process.ProcessVo;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 审批类型 Mapper 接口
 * </p>
 *
 * @author SingKwan
 * @since 2023-07-22
 */
public interface OaProcessMapper extends BaseMapper<Process> {
    IPage<ProcessVo> selectPage(Page<ProcessVo> pageParam, @Param("vo") ProcessQueryVo processQueryVo);
}
