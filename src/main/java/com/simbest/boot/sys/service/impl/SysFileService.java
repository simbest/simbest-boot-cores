package com.simbest.boot.sys.service.impl;

import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Lists;
import com.simbest.boot.base.enums.StoreLocation;
import com.simbest.boot.base.exception.Exceptions;
import com.simbest.boot.base.service.impl.LogicService;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.exceptions.AppRuntimeException;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.sys.model.UploadFileResponse;
import com.simbest.boot.sys.repository.SysFileRepository;
import com.simbest.boot.sys.service.ISysFileService;
import com.simbest.boot.util.AppFileUtil;
import com.simbest.boot.util.CodeGenerator;
import com.simbest.boot.util.SpringContextUtil;
import com.simbest.boot.util.office.ExcelUtil;
import com.simbest.boot.util.security.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * 用途：统一系统文件管理逻辑层
 * 作者: lishuyi
 * 时间: 2018/2/23  10:14
 */
@Slf4j
@Service
@DependsOn(value = {"appFileUtil"})
public class SysFileService extends LogicService<SysFile, String> implements ISysFileService {

    public static final String FILE_ERROR = "文件操作异常【%s】";

    @Autowired
    private SysFileRepository repository;

    @Autowired
    private AppFileUtil appFileUtil;

    @Autowired
    private AppConfig config;

    @Autowired
    private SpringContextUtil springContextUtil;

    public StoreLocation serverUploadLocation;

    @Autowired
    public SysFileService(SysFileRepository repository) {
        super(repository);
        this.repository = repository;
    }

    @PostConstruct
    public void init() {
        serverUploadLocation = Enum.valueOf(StoreLocation.class, config.getUploadLocation());
    }

    @Override
    public SysFile uploadProcessFile(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> fileList = uploadProcessFiles(Arrays.asList(multipartFile), pmInsType, pmInsId, pmInsTypePart);
        return fileList.isEmpty() ? null : fileList.get(0);
    }

    @Override
    @Transactional
    public List<SysFile> uploadProcessFiles(Collection<MultipartFile> multipartFiles, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> sysFileList;
        try {
            sysFileList = appFileUtil.uploadFiles(prepareDirectory(pmInsType, pmInsId, pmInsTypePart), multipartFiles);
            saveSysFileList(pmInsType, pmInsId, pmInsTypePart, sysFileList);
        } catch (Exception e) {
            throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
        }
        return sysFileList;
    }

    @Override
    public SysFile uploadProcessFile (MultipartFile multipartFile,String customFileName,String customDirectory,String pmInsType, String pmInsId, String pmInsTypePart ) {
        List<SysFile> fileList = uploadProcessFiles(Arrays.asList(multipartFile),customFileName, customDirectory, pmInsType, pmInsId, pmInsTypePart);
        return fileList.isEmpty() ? null : fileList.get(0);
    }

    @Override
    public List<SysFile> uploadProcessFiles (Collection<MultipartFile> multipartFiles,String customFileName,String customDirectory,String pmInsType, String pmInsId, String pmInsTypePart ) {
        List<SysFile> sysFileList;
        try {
            sysFileList = appFileUtil.customUploadFiles(prepareCustomDirectory(customDirectory, pmInsType, pmInsId, pmInsTypePart), multipartFiles, customFileName);
            saveSysFileList(pmInsType, pmInsId, pmInsTypePart, sysFileList);
        }catch (Exception e) {
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
        }
        return sysFileList;
    }

    @Override
    public SysFile uploadLocalProcessFile(File localFile, String pmInsType, String pmInsId, String pmInsTypePart) {
        SysFile sysFile = null;
        try {
            sysFile = appFileUtil.uploadFromLocalAutoDirectory(prepareDirectory(pmInsType, pmInsId, pmInsTypePart), localFile, null);
            saveSysFileList(pmInsType, pmInsId, pmInsTypePart, Arrays.asList(sysFile));
        } catch (Exception e) {
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
        }
        return sysFile;
    }

    @Override
    public List<SysFile> uploadLocalProcessFiles(Collection<File> localFiles, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> sysFileList = Lists.newArrayList();
        for(File localFile : localFiles){
            SysFile sysFile = uploadLocalProcessFile(localFile, pmInsType, pmInsId, pmInsTypePart);
            if(null != sysFile){
                sysFileList.add(sysFile);
            }
        }
        return sysFileList;
    }

    @Override
    public SysFile uploadLocalProcessFile(File localFile, String customFileName, String customDirectory, String pmInsType, String pmInsId, String pmInsTypePart) {
        SysFile sysFile = null;
        try {
            sysFile = appFileUtil.uploadFromLocalCustomDirectory(prepareCustomDirectory(customDirectory, pmInsType, pmInsId, pmInsTypePart), localFile, customFileName);
            saveSysFileList(pmInsType, pmInsId, pmInsTypePart, Arrays.asList(sysFile));
        } catch (Exception e) {
            Exceptions.printException(e);
            throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
        }
        return sysFile;
    }

    @Override
    public List<SysFile> uploadLocalProcessFiles(Collection<File> localFiles, String customFileName, String customDirectory, String pmInsType, String pmInsId, String pmInsTypePart) {
        List<SysFile> sysFileList = Lists.newArrayList();
        for(File localFile : localFiles){
            SysFile sysFile = uploadLocalProcessFile(localFile, customFileName, customDirectory, pmInsType, pmInsId, pmInsTypePart);
            if(null != sysFile){
                sysFileList.add(sysFile);
            }
        }
        return sysFileList;
    }

    private String prepareDirectory(String pmInsType, String pmInsId, String pmInsTypePart){
        String pmInsTypePath = StrUtil.isEmpty(pmInsType) ? "" : pmInsType.concat(ApplicationConstants.SLASH);
        String pmInsIdPath = StrUtil.isEmpty(pmInsId) ? "" : pmInsId.concat(ApplicationConstants.SLASH);
        String pmInsTypePartPath = StrUtil.isEmpty(pmInsTypePart) ? "" : pmInsTypePart.concat(ApplicationConstants.SLASH);
        String username = SecurityUtils.getCurrentUserName();
        String directory = StringUtils.removeEnd(pmInsTypePath + username + ApplicationConstants.SLASH
                + CodeGenerator.systemUUID() + ApplicationConstants.SLASH
                + pmInsTypePartPath + pmInsIdPath , ApplicationConstants.SLASH);
        log.debug("上传路径地址为【{}】", directory);
        return directory;
    }

    private String prepareCustomDirectory(String customDirectory, String pmInsType, String pmInsId, String pmInsTypePart){
        customDirectory = customDirectory + ApplicationConstants.SLASH + prepareDirectory(pmInsType, pmInsId, pmInsTypePart);
        customDirectory = StringUtils.removeEnd(customDirectory, ApplicationConstants.SLASH);
        log.debug("自定义上传路径地址为【{}】", customDirectory);
        return customDirectory;
    }

    private void saveSysFileList(String pmInsType, String pmInsId, String pmInsTypePart, List<SysFile> sysFileList){
        for(SysFile sysFile : sysFileList){
            sysFile = super.insert(sysFile); //先保存文件获取ID
            sysFile.setDownLoadUrl(sysFile.getDownLoadUrl().concat("?id="+sysFile.getId())); //修改下载URL，追加ID
            sysFile.setApiFilePath(sysFile.getApiFilePath().concat("?id="+sysFile.getId()));
            sysFile.setAnonymousFilePath(sysFile.getAnonymousFilePath().concat("?id="+sysFile.getId()));
            sysFile.setPmInsType(pmInsType);
            sysFile.setPmInsId(pmInsId);
            sysFile.setPmInsTypePart(pmInsTypePart);
            String mobileFilePath = null;
            String apiFilePath = null;
            String anonymousFilePath = null;
            switch (serverUploadLocation) {
                case fastdfs:
                    mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + sysFile.getFilePath();
                    apiFilePath = mobileFilePath;
                    anonymousFilePath = mobileFilePath;
                    break;
                case disk:
                case ftp:
                case sftp:
                    mobileFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getDownLoadUrl();
                    apiFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getApiFilePath();
                    anonymousFilePath = config.getAppHostPort() + ApplicationConstants.SLASH + config.getAppcode() + sysFile.getAnonymousFilePath();
                    break;
            }
            sysFile.setMobileFilePath( mobileFilePath );
            sysFile.setApiFilePath(apiFilePath);
            sysFile.setAnonymousFilePath(anonymousFilePath);
            super.update(sysFile); //再保存一下更新的值
        }
    }

    @Override
    public <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                List<T> listData = importUtil.importExcel(sheetName, new FileInputStream(tempFile));
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setListData(listData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
                throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
            }
        }
        return null;
    }

    @Override
    public <T> UploadFileResponse importExcel ( MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz, String sheetName, int inputRow ) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                List<T> listData = importUtil.importExcel(sheetName, new FileInputStream(tempFile),inputRow);
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setListData(listData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
                throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
            }
        }
        return null;
    }

    @Override
    public <T> UploadFileResponse importExcel(MultipartFile multipartFile, String pmInsType, String pmInsId, String pmInsTypePart, Class<T> clazz) {
        SysFile sysFile = uploadProcessFile(multipartFile, pmInsType, pmInsId, pmInsTypePart);
        if (sysFile != null) {
            ExcelUtil<T> importUtil = new ExcelUtil<>(clazz);
            File tempFile = appFileUtil.createTempFile();
            try {
                multipartFile.transferTo(tempFile);
                Map<String, List<T>> mapData = importUtil.importExcel(new FileInputStream(tempFile));
                UploadFileResponse<T> uploadFileResponse = new UploadFileResponse<>();
                uploadFileResponse.setMapData(mapData);
                uploadFileResponse.setSysFiles(Arrays.asList(sysFile));
                return uploadFileResponse;
            } catch (IOException e) {
                Exceptions.printException(e);
                throw new AppRuntimeException(String.format(FILE_ERROR, e.getMessage()));
            }
        }
        return null;
    }

    @Override
    public File getRealFileById(String id) {
        SysFile sysFile = this.findById(id);
        return appFileUtil.getFileFromSystem(sysFile);
    }

    @Override
    @Transactional
    public void deleteById ( String id ) {
        SysFile sysFile = this.findById(id);
        String filePath = sysFile.getFilePath();
        super.deleteById(id);
        boolean result = appFileUtil.deleteFile(sysFile);
        log.warn("物理删除文件结果为【{}】", result);
    }

}
