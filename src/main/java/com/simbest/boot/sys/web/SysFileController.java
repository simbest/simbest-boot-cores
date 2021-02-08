/*
 * 版权所有 © 北京晟壁科技有限公司 2008-2027。保留一切权利!
 */
package com.simbest.boot.sys.web;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.simbest.boot.base.web.controller.LogicController;
import com.simbest.boot.base.web.response.JsonResponse;
import com.simbest.boot.config.AppConfig;
import com.simbest.boot.constants.ApplicationConstants;
import com.simbest.boot.sys.model.SysFile;
import com.simbest.boot.sys.model.UploadFileResponse;
import com.simbest.boot.sys.service.ISysFileService;
import com.simbest.boot.util.AppFileUtil;
import com.simbest.boot.util.UrlEncoderUtils;
import com.simbest.boot.util.encrypt.UrlEncryptor;
import com.simbest.boot.util.encrypt.WebOffice3Des;
import com.simbest.boot.util.http.BrowserUtil;
import com.simbest.boot.util.json.JacksonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.simbest.boot.util.AppFileUtil.NGINX_STATIC_FILE_LOCATION;

/**
 * 用途：统一系统文件管理控制器
 * 作者: lishuyi https://www.mkyong.com/spring-boot/spring-boot-file-upload-example-ajax-and-rest/
 * 时间: 2018/2/23  10:14
 */
@Api(description = "SysFileController", tags = {"系统管理-文件管理"})
@Slf4j
@Controller
@RequestMapping("/sys/file")
public class SysFileController extends LogicController<SysFile, String> {

    public final static String UPLOAD_PROCESS_FILES_URL = "/uploadProcessFiles";
    public final static String UPLOAD_PROCESS_FILES_URL_SSO = "/uploadProcessFiles/sso";
    public final static String UPLOAD_PROCESS_FILES_URL_API = "/uploadProcessFiles/api";
    public final static String UPLOAD_PROCESS_FILES_URL_REST = "/uploadProcessFiles/rest";
    public final static String UPLOAD_PROCESS_FILES_URL_REST_SSO = "/uploadProcessFiles/rest/sso";
    public final static String UPLOAD_PROCESS_FILES_URL_REST_API = "/uploadProcessFiles/rest/api";
    public final static String DOWNLOAD_URL = "/download";
    public final static String DOWNLOAD_URL_SSO = "/download/sso";
    public final static String DOWNLOAD_URL_API = "/download/api";
    public final static String DOWNLOAD_URL_ANONYMOUSI = "/download/anonymous";
    public final static String DOWNLOAD_FULL_URL = "/sys/file/download";
    public final static String DOWNLOAD_FULL_URL_API = "/sys/file/download/api";
    public final static String DOWNLOAD_FULL_URL_ANONYMOUS = "/sys/file/download/anonymous";
    public final static String OPEN_URL = "/open";
    public final static String OPEN_URL_SSO = "/open/sso";
    public final static String OPEN_URL_API = "/open/api";
    public final static String DELETE_URL = "/deleteById";

    @Autowired
    private ISysFileService fileService;

    @Autowired
    private UrlEncryptor urlEncryptor;

    @Autowired
    private AppFileUtil appFileUtil;

    @Setter
    @Autowired
    private AppConfig config;

    @Autowired
    public SysFileController(ISysFileService fileService) {
        super(fileService);
        this.fileService = fileService;
    }

    @PostMapping(value = {"/uploadFile", "/uploadFile/sso", "/uploadFile/api"})
    @ResponseBody
    public JsonResponse uploadFile(@RequestParam("file") MultipartFile file, @RequestParam(value = "pmInsType", required = false) String pmInsType,
                                   @RequestParam(value = "pmInsId", required = false) String pmInsId,
                                   @RequestParam(value = "pmInsTypePart", required = false) String pmInsTypePart) {
        SysFile sysFile = fileService.uploadProcessFile(file, pmInsType, pmInsId, pmInsTypePart);
        JsonResponse jsonResponse;
        if (null != sysFile) {
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setSysFiles(ImmutableList.of(sysFile));
            jsonResponse = JsonResponse.success(uploadFileResponse);
        } else {
            jsonResponse = JsonResponse.defaultErrorResponse();
        }
        return jsonResponse;
    }

    @ApiOperation(value = "传统方式上传附件（支持IE8）,支持关联流程", notes = "会保存到数据库SYS_FILE")
    @PostMapping(value = {UPLOAD_PROCESS_FILES_URL, UPLOAD_PROCESS_FILES_URL_SSO, UPLOAD_PROCESS_FILES_URL_API})
    @ResponseBody
    public void uploadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JsonResponse jsonResponse = doUploadFile(request, response);
        String result = "<script type=\"text/javascript\">parent.result=" + JacksonUtils.obj2json(jsonResponse) + "</script>";
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out = response.getWriter();
        out.println(result);
        out.close();
    }

    @ApiOperation(value = "REST方式上传附件,支持关联流程", notes = "会保存到数据库SYS_FILE")
    @PostMapping(value = {UPLOAD_PROCESS_FILES_URL_REST, UPLOAD_PROCESS_FILES_URL_REST_SSO, UPLOAD_PROCESS_FILES_URL_REST_API})
    @ResponseBody
    public ResponseEntity<?> uploadFileRest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        JsonResponse jsonResponse = doUploadFile(request, response);
        return new ResponseEntity(jsonResponse, HttpStatus.OK);
    }

    /**
     * 上传文件,支持关联流程
     */
    private JsonResponse doUploadFile(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Set<MultipartFile> uploadingFileSet = Sets.newHashSet();
        MultipartHttpServletRequest mureq = (MultipartHttpServletRequest) request;
        //优先通过指定参数名称file获取文件
        Collection<MultipartFile> uploadingFileList = mureq.getFiles("file");
        uploadingFileList.forEach(f -> uploadingFileSet.add(f));
        //再通过不指定参数名称获取文件
        Map<String, MultipartFile> multipartFiles = mureq.getFileMap();
        multipartFiles.values().forEach(f -> uploadingFileSet.add(f));
        List<SysFile> sysFiles = fileService.uploadProcessFiles(uploadingFileSet,
                request.getParameter("pmInsType"),
                request.getParameter("pmInsId"),
                request.getParameter("pmInsTypePart"));
        JsonResponse jsonResponse;
        if (!sysFiles.isEmpty()) {
            UploadFileResponse uploadFileResponse = new UploadFileResponse();
            uploadFileResponse.setSysFiles(sysFiles);
            jsonResponse = JsonResponse.success(uploadFileResponse);
        } else {
            jsonResponse = JsonResponse.defaultErrorResponse();
        }
        return jsonResponse;
    }


    /**
     * 下载视频文件(解决iOS操作系统播放MP4)
     * 参考：https://blog.csdn.net/weixin_42553179/article/details/100008911
     * @param request
     * @param id
     * @throws IOException
     */
    @GetMapping(value = {"/downloadVideo", "/downloadVideo/sso", "/downloadVideo/api", "/downloadVideo/anonymous"})
    @ResponseBody
    public void downloadVideo(HttpServletRequest request, HttpServletResponse response, @RequestParam("id") String id) throws IOException {
        SysFile sysFile = fileService.findById(id);
        Assert.notNull(sysFile, String.format("通过文件Id【%s】无法获取文件", id));
        //获取真实文件
        File realFile = fileService.getRealFileById(id);
        RandomAccessFile randomFile = new RandomAccessFile(realFile, "r");//只读模式
        long contentLength = randomFile.length();
        String range = request.getHeader("Range");
        int start = 0, end = 0;
        if (range != null && range.startsWith("bytes=")) {
            String[] values = range.split("=")[1].split("-");
            start = Integer.parseInt(values[0]);
            if (values.length > 1) {
                end = Integer.parseInt(values[1]);
            }
        }
        int requestSize = 0;
        if (end != 0 && end > start) {
            requestSize = end - start + 1;
        } else {
            requestSize = Integer.MAX_VALUE;
        }
        String fileType = AppFileUtil.getFileType(realFile); //支持 video/mp4
        log.debug("下载文件【{}】类型为【{}】", sysFile.getFileName(), fileType);
//        response.setContentType("video/mp4");
        response.setContentType(fileType);
        response.setHeader("Accept-Ranges", "bytes");
        response.setHeader("ETag", sysFile.getFileName());
        response.setHeader("Last-Modified", new Date().toString());
        //第一次请求只返回content length来让客户端请求多次实际数据
        if (range == null) {
            log.debug("分段请求Range区间【{}】", range);
            response.setHeader("Content-length", contentLength + "");
        } else {
            log.debug("分段请求Range区间【{}】", range);
            //以后的多次以断点续传的方式来返回视频数据
            response.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);//206
            long requestStart = 0, requestEnd = 0;
            String[] ranges = range.split("=");
            if(ranges.length > 1){
                String[] rangeDatas = ranges[1].split("-");
                requestStart = Integer.parseInt(rangeDatas[0]);
                if(rangeDatas.length > 1){
                    requestEnd = Integer.parseInt(rangeDatas[1]);
                }
            }
            long length = 0;
            if(requestEnd > 0){
                length = requestEnd - requestStart + 1;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes " + requestStart + "-" + requestEnd + "/" + contentLength);
            }else{
                length = contentLength - requestStart;
                response.setHeader("Content-length", "" + length);
                response.setHeader("Content-Range", "bytes "+ requestStart + "-" + (contentLength - 1) + "/" + contentLength);
            }
        }
        ServletOutputStream out = response.getOutputStream();
        int needSize = requestSize;
        randomFile.seek(start);
        while(needSize > 0){
            byte[] buffer = new byte[4096];
            int len = randomFile.read(buffer);
            if(needSize < buffer.length){
                out.write(buffer, 0, needSize);
            } else {
                out.write(buffer, 0, len);
                if(len < buffer.length){
                    break;
                }
            }
            needSize -= buffer.length;
        }
        randomFile.close();
        out.close();
    }

    /**
     * 下载文件(图片和通用文档)
     *
     * @param request
     * @param id
     * @return JsonResponse
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     */
    @ApiOperation(value = "下载文件")
    @GetMapping(value = {DOWNLOAD_URL, DOWNLOAD_URL_SSO, DOWNLOAD_URL_API, DOWNLOAD_URL_ANONYMOUSI})
    @ResponseBody
    public ResponseEntity<?> download(HttpServletRequest request, @RequestParam("id") String id) throws IOException {
        SysFile sysFile = fileService.findById(id);
        Assert.notNull(sysFile, String.format("通过文件Id【%s】无法获取文件", id));
        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        boolean isMSIE = BrowserUtil.isMSBrowser(request);
        String fileName;
        if (isMSIE) {
            fileName = URLEncoder.encode(sysFile.getFileName(), ApplicationConstants.UTF_8);
        } else {
            fileName = new String(sysFile.getFileName().getBytes(ApplicationConstants.UTF_8), "ISO-8859-1");
        }
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"");

        //获取真实文件
        File realFile = fileService.getRealFileById(id);
        //设置文件类型
        String fileType = AppFileUtil.getFileType(realFile); //image/jpeg video/mp4
        log.debug("下载文件【{}】类型为【{}】", sysFile.getFileName(), fileType);
        String[] fileTypes = StringUtils.split(fileType, ApplicationConstants.SLASH);
        //1-响应图片
        if (AppFileUtil.isImage(realFile)) {
            headers.setContentType(new MediaType(fileTypes[0], fileTypes[1]));
        }
        //2-响应视频--iOS操作系统无法播放，可使用/downloadVideo
        else if ("video".equals(fileTypes[0])) {
            headers.setContentType(new MediaType(fileTypes[0], fileTypes[1]));
        }
        //3-响应文件流
        else{
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }
//        headers.setContentLength(sysFile.getFileSize());
        RandomAccessFile randomFile = new RandomAccessFile(realFile, "r");//只读模式
        headers.setContentLength(randomFile.length());
        Resource resource = new InputStreamResource(new FileInputStream(realFile));
        return ResponseEntity.ok().headers(headers).body(resource);
    }


        /**
         * 在线预览文件，但依赖于Nginx
         * @param id
         * @param uploadPath
         * @return String
         */
        @GetMapping(value = {"/ngopen", "/ngopen/sso", "/ngopen/api"})
        public String ngopen (@RequestParam("id") String
        id, @RequestParam(value = "uploadPath", required = false) String uploadPath){
            SysFile sysFile = fileService.findById(id);
            Assert.notNull(sysFile, String.format("通过文件Id【%s】无法获取文件", id));
            log.debug("尝试预览文件地址为【{}】", sysFile.getFilePath());
            if (StringUtils.isEmpty(uploadPath)) {
                uploadPath = config.getUploadPath();
            }
            String nginxUrl = config.getAppHostPort() + NGINX_STATIC_FILE_LOCATION + StringUtils.remove(sysFile.getFilePath(), uploadPath);
            log.debug("转换后webOfficeUrl地址为【{}】", nginxUrl);
            return "redirect:" + nginxUrl;
        }


        /**
         * 在线预览文件，仅适用于保存在FastDfs环境中的文件, 并且依赖http://www.officeweb365.com/
         * @param id
         * @return String
         * @throws Exception
         */
        @GetMapping(value = {OPEN_URL, OPEN_URL_SSO, OPEN_URL_API})
        public String open (@RequestParam("id") String id) throws Exception {
            SysFile sysFile = fileService.findById(id);
            Assert.notNull(sysFile, String.format("通过文件Id【%s】无法获取文件", id));
            log.debug("尝试预览文件地址为【{}】", sysFile.getFilePath());
            String fastDfsUrl = config.getAppHostPort() + ApplicationConstants.SLASH + sysFile.getFilePath();
            String redirectUrl = getOfficeweb365Url(fastDfsUrl);
            log.debug("转换后webOfficeUrl地址为【{}】", redirectUrl);
            return "redirect:" + redirectUrl;
        }

        /**
         * 在线预览文件，支持任意免认证的URL, 并且依赖http://www.officeweb365.com/
         * @param url
         * @return String
         * @throws Exception
         */
        @RequestMapping(value = {"/openurl", "/openurl/sso", "/openurl/api"}, method = {RequestMethod.POST, RequestMethod.GET})
        public String openurl (@RequestParam String url) throws Exception {
            return "redirect:" + getOfficeweb365Url(url);
        }

        /**
         * 在线预览文件，支持任意免认证的URL,不进行重定向, 并且依赖http://www.officeweb365.com/
         * @param url
         * @return
         * @throws Exception
         */
        @RequestMapping(value = {"/get/url", "/get/url/sso", "/get/url/api"}, method = {RequestMethod.POST, RequestMethod.GET})
        @ResponseBody
        public ResponseEntity openurlNoRedirect (@RequestParam String url) throws Exception {
            return new ResponseEntity(JsonResponse.success(getOfficeweb365Url(url)), HttpStatus.OK);
        }

        private String getOfficeweb365Url (String url) throws Exception {
            if (UrlEncoderUtils.hasUrlEncoded(url)) {
                url = urlEncryptor.decrypt(url);
            }
            log.debug("尝试预览文件地址为【{}】", url);
            String redirectUrl = config.getAppHostPort() + "/webOffice/?furl=" + WebOffice3Des.encode(url);
            log.debug("转换后webOfficeUrl地址为【{}】", redirectUrl);
            return redirectUrl;
        }

        @PostMapping(value = DELETE_URL)
        @ResponseBody
        public JsonResponse deleteById (@RequestParam("id") String id){
            fileService.deleteById(id);
            return JsonResponse.defaultSuccessResponse();
        }

        @Override
        @ResponseBody
        @PostMapping(value = {"/update", "/update/api", "/update/sso"})
        public JsonResponse update (@RequestBody SysFile sysFile){
            JsonResponse jsonResponse = super.update(sysFile);
            return jsonResponse;
        }

//    /**
//     * 涉及到具体对象的操作，所以不直接暴露接口
//     *
//     * @param uploadfile
//     * @param pmInsType
//     * @param pmInsId
//     * @param pmInsTypePart
//     * @param clazz
//     * @param <T>
//     * @throws IOException
//     * @rn
//     */
//    private <T> JsonResponse importExcel(MultipartFile uploadfile,
//                                         String pmInsType,
//                                         String pmInsId, //起草阶段上传文件，可不填写业务单据ID
//                                         String pmInsTypePart,
//                                         Class<T> clazz) throws IOException {
//        UploadFileResponse uploadFileResponse = fileService.importExcel(uploadfile, pmInsType, pmInsId, pmInsTypePart, clazz);
//        if (null != uploadFileResponse) {
//            return JsonResponse.success(uploadFileResponse);
//        } else {
//            return JsonResponse.defaultErrorResponse();
//        }
//    }

        public static void main (String[]args) throws Exception {
            WebOffice3Des webOffice3Des = new WebOffice3Des();
            AppConfig config = new AppConfig();
            config.setAppHostPort("http://211.138.31.210:8088");
            SysFileController sysFileController = new SysFileController(null);
            sysFileController.setConfig(config);
            String url = "http://10.87.13.91:8888/20200525/DemoExcel.xlsx";
            System.out.println(sysFileController.getOfficeweb365Url(url));
            url = "http://10.87.13.91:8888/20200525/DemoPdf.pdf";
            System.out.println(sysFileController.getOfficeweb365Url(url));
            url = "http://10.87.13.91:8888/20200525/DemoPpt.pptx";
            System.out.println(sysFileController.getOfficeweb365Url(url));
            url = "http://10.87.13.91:8888/20200525/DemoWord.doc";
            System.out.println(sysFileController.getOfficeweb365Url(url));
        }

    }
