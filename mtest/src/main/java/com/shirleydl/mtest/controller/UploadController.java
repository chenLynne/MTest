/**
 * @description 用例库-导入文件
 * @author 371683941@qq.com
 * @date 2021/5/10
 */
package com.shirleydl.mtest.controller;

import com.shirleydl.mtest.method.CheckFileUtil;
import com.shirleydl.mtest.method.ExcelUtils;
import com.shirleydl.mtest.method.XMindUtils;
import com.shirleydl.mtest.service.*;
import com.shirleydl.mtest.vo.ExportInfo;
import com.shirleydl.mtest.vo.ResponseVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/caseLibrary")
public class UploadController {
    @Autowired
    private ISystemsService systemsService;
    @Autowired
    private IPageModulesService pageModulesService;
    @Autowired
    private IFunctionsService functionsService;
    @Autowired
    private ITestPointService testPointService;
    @Autowired
    private ITestContentService testContentService;

    /**
     * 导入文件
     * @param file
     * @return
     */
    @PostMapping("/upload")
    public ResponseVO upload(@RequestParam("file") MultipartFile file) {
        try {
            List<ExportInfo> exportInfoList = new ArrayList<>();
            //获取文件类型
            Integer type = new CheckFileUtil().getFileType(file);

            if (type == 1) {
                exportInfoList = new ExcelUtils().readExcelToExportInfo(file);
            } else if (type == 2) {
                exportInfoList = new XMindUtils().readXMindToExportInfo(file);
            } else {
                return ResponseVO.failure("导入失败，格式不支持！");
            }

            Map<String, Integer> systemsMap = new HashMap<>();
            Map<String, Integer> pageModulesMap = new HashMap<>();
            Map<String, Integer> functionsMap = new HashMap<>();
            Map<String, Integer> testPointMap = new HashMap<>();
            Set<String> content = new HashSet<>();
            int systemsId;
            int pageModulesId;
            int functionsId;
            int testPointId;
            for (ExportInfo exportInfo : exportInfoList) {
                if (systemsMap.containsKey(exportInfo.getSystemsName())) {
                    systemsId = systemsMap.get(exportInfo.getSystemsName());
                } else {
                    systemsId = systemsService.addOrFindSystems(exportInfo.getSystemsName());
                    systemsMap.put(exportInfo.getSystemsName(), systemsId);
                }
                if (pageModulesMap.containsKey(systemsId + exportInfo.getPageModulesName())) {
                    pageModulesId = pageModulesMap.get(systemsId + exportInfo.getPageModulesName());
                } else {
                    pageModulesId = pageModulesService.addOrFindPageModules(exportInfo.getPageModulesName(), systemsId);
                    pageModulesMap.put(systemsId + exportInfo.getPageModulesName(), pageModulesId);
                }
                if (functionsMap.containsKey(pageModulesId + exportInfo.getFunctionsName())) {
                    functionsId = functionsMap.get(pageModulesId + exportInfo.getFunctionsName());
                } else {
                    functionsId = functionsService.addOrFindFunctions(exportInfo.getFunctionsName(), pageModulesId);
                    functionsMap.put(pageModulesId + exportInfo.getFunctionsName(), functionsId);
                }
                if (testPointMap.containsKey(functionsId + exportInfo.getTestPointName())) {
                    testPointId = testPointMap.get(functionsId + exportInfo.getTestPointName());
                } else {
                    testPointId = testPointService.addOrFindTestPoint(exportInfo.getTestPointName(), functionsId);
                    functionsMap.put(functionsId + exportInfo.getTestPointName(), testPointId);
                }
                if (!content.contains(testPointId + exportInfo.getPre() + exportInfo.getStep())) {
                    testContentService.addOrFindTestContent(exportInfo.getPre(), exportInfo.getStep(), exportInfo.getExpect(), testPointId);
                    content.add(testPointId + exportInfo.getPre() + exportInfo.getStep());
                }

//                System.out.print(exportInfo.getSystemsName() + " ");
//                System.out.print(exportInfo.getPageModulesName() + " ");
//                System.out.print(exportInfo.getFunctionsName() + " ");
//                System.out.print(exportInfo.getTestPointName() + " ");
//                System.out.println("");
            }
            return ResponseVO.success("导入成功");
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseVO.failure("导入失败");
        }

    }

}
