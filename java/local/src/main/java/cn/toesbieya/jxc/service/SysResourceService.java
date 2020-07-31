package cn.toesbieya.jxc.service;

import cn.toesbieya.jxc.model.entity.SysResource;
import cn.toesbieya.jxc.mapper.SysResourceMapper;
import cn.toesbieya.jxc.module.request.RequestModule;
import cn.toesbieya.jxc.model.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;

@Service
@Slf4j
public class SysResourceService {
    @Resource
    private SysResourceMapper resourceMapper;

    public List<SysResource> get() {
        List<SysResource> list = resourceMapper.get();
        completeNode(list);
        return list;
    }

    public List<SysResource> getAll() {
        List<SysResource> list = resourceMapper.getAll();
        completeNode(list);
        return list;
    }

    public List<SysResource> getByRole(int role) {
        List<SysResource> list = resourceMapper.getByRole(role);
        completeNode(list);
        return list;
    }

    public Result update(SysResource resource) {
        int rows = resourceMapper.update(resource);
        if (rows > 0) {
            RequestModule.updateRate(resource.getUrl(), resource.getTotalRate(), resource.getIpRate());
        }
        return Result.success("修改成功");
    }

    private void completeNode(List<SysResource> list) {
        HashMap<Integer, String> url = new HashMap<>(128);
        HashMap<Integer, String> name = new HashMap<>(128);

        for (SysResource resource : list) {
            //pid为0时跳过
            if (resource.getPid() == 0) {
                url.put(resource.getId(), resource.getUrl());
                name.put(resource.getId(), resource.getName());
                resource.setFullname(resource.getName());
                continue;
            }

            //获取父节点进行拼接
            String parentUrl = url.get(resource.getPid());
            String parentName = name.get(resource.getPid());
            assert !StringUtils.isEmpty(parentUrl) && !StringUtils.isEmpty(parentName);

            resource.setUrl(parentUrl + resource.getUrl());
            resource.setFullname(parentName + " - " + resource.getName());
            url.put(resource.getId(), resource.getUrl());
            name.put(resource.getId(), resource.getFullname());
        }
    }
}
