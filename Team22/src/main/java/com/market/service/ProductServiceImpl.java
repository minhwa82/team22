package com.market.service;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.market.domain.PCriteria;
import com.market.domain.ProductVO;
import com.market.persistence.ProductDAO;

import net.coobird.thumbnailator.Thumbnails;

@Service
public class ProductServiceImpl implements ProductService{
	
	private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);
	@Autowired
	private ProductDAO pdao;
	
	@Override
	public List<ProductVO> getProdList(String grade, String category, String title, String sort) {
		logger.info("service - 상품 목록 호출");
		Map<String, Object> map = new HashMap<>();
		map.put("grade", grade);
		map.put("category", category);
		map.put("title", title);
		map.put("sortBy", sort);
		return pdao.getProdList(map);
	}

	@Override
	public List<ProductVO> getProdListPage(String grade, String category, String title, String sort,
			Integer min, Integer max, PCriteria cri) {
		Map<String, Object> map = new HashMap<>();
		map.put("grade", grade);
		map.put("category", category);
		map.put("title", title);
		map.put("sortBy", sort);
		map.put("min", min);
		map.put("max", max);
		map.put("pageSize", cri.getPageBlock());
		map.put("startPage", (cri.getPageNum()-1) * cri.getPageBlock());
		logger.info("service - 상품 목록 페이지 호출");
		logger.info(map.toString());
		return pdao.getProdListPage(map);
	}

	@Override
	public void likeProd(int pnum) {
		pdao.likeProd(pnum);
	}

	@Override
	public void likeProdCancel(int pnum) {
		pdao.likeProdCancel(pnum);
	}

	@Override
	public int likeExist(int pnum, String seller) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pnum", pnum);
		map.put("id", seller);
		return pdao.likeExist(map);
	}
	
	@Override
	public void regFavorite(int pnum, String seller) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pnum", pnum);
		map.put("id", seller);
		pdao.regFavorite(map);
	}

	@Override
	public void delFavorite(int pnum, String seller) {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("pnum", pnum);
		map.put("id", seller);
		pdao.delFavorite(map);
	}


	@Override
	public Map<String, Object> getProdInfo(Integer pnum) {
		logger.info("service - 상품 정보 가져오기");
		return pdao.getProdInfo(pnum);
	}

	@Override
	public List<ProductVO> getRecProdList(String category) {
		return pdao.getRecProdList(category);
	}

	@Override
	public double getScore(String name) {
		return pdao.getScore(name);
	}

	@Override
	public int regProduct(ProductVO vo
						, MultipartFile[] files) throws Exception {
		logger.info("service - 상품 등록");
		// String currentWorkingDirectory = new File(".").getCanonicalPath(); -> Team22까지
		String path = "C:\\Users\\ITWILL\\git\\team22\\Team22\\src\\main\\webapp\\resources\\images";
		StringBuilder sb = new StringBuilder();
		
		
		for(MultipartFile file : files) {
			if(!file.isEmpty()) {
			    UUID uuid = UUID.randomUUID();
			    String origin = file.getOriginalFilename();
//			    String fileName = uuid + "-" + origin;
			    String fileName = origin;
			    File saveFile = new File(path, fileName);
			    file.transferTo(saveFile);
			    
			    // Thumbnail part
			    
//			    File thumbnail = new File(path+"\\thumb", "thumb_" + fileName);
//			    
//			    BufferedImage bo_image = ImageIO.read(file.getInputStream());
//			    
//			    double ratio = 3;
//	            int width = (int) (bo_image.getWidth() / ratio);
//	            int height = (int) (bo_image.getHeight() / ratio);
//	            
//	            Thumbnails.of(saveFile).size(width, height).toFile(thumbnail);
			    
			    // Thumbnail part
			    
			    if(sb.length() > 0) {
			    	sb.append(",");
			    }
			    sb.append(fileName);
			}
		}
		
	    vo.setProduct_pic(sb.toString());

	    logger.info("vo : " + vo.toString());
		pdao.regProduct(vo);
		
		return pdao.getLastProdNum(vo.getProduct_seller());
	}

	@Override
	public void regProduct(ProductVO vo, MultipartHttpServletRequest request) throws Exception {
		List<MultipartFile> files = request.getFiles("product_pics");
		String path = new File(".").getCanonicalPath() + "\\src\\main\\webapp\\resources\\images";
		
		List<String> fileNames = fileProcess(request, path);
	}
	
	@Override
	public void incView(HttpServletRequest request, HttpServletResponse response, Integer product_num) {
		String viewedCookieName = "vc" + product_num;
		Cookie[] cookies = request.getCookies();
		boolean viewCheck = true;
		
		if(cookies != null) {
			for (Cookie cookie : cookies) {
				if (viewedCookieName.equals(cookie.getName())) {
					viewCheck = false;
					break;
				}
			}
		}
		
		if(viewCheck) {
			pdao.incView(product_num);
			
			Cookie viewedCookie = new Cookie(viewedCookieName, "true");
			viewedCookie.setMaxAge(24*60*60);
			viewedCookie.setPath("/");
			response.addCookie(viewedCookie);
		}
	}


	// 파일 업로드 처리
		private List<String> fileProcess(MultipartHttpServletRequest multiRequest
										,String path) throws Exception {
			// 파일이름 저장하는 리스트
			List<String> fileList = new ArrayList<String>();
			
			// input-file 파라미터 정보 가져오기
			Iterator<String> fileNames = multiRequest.getFileNames();
			while(fileNames.hasNext()) {
				String fileName = fileNames.next();
				
				// 이름에 해당하는 실제 파일의 데이터를 저장
				MultipartFile mFile = multiRequest.getFile(fileName);
				// 파일의 이름
				String oFileName = mFile.getOriginalFilename();
				fileList.add(oFileName);
				
				File file = new File(path + fileName);
				// 파일의 내용 추가
				// 업로드한 파일이 있을 때
				if(mFile.getSize() != 0) {
					//해당 경로에 파일이 없을 경우
					if(!file.exists()) {
						// 해당하는 디렉토리 생성 후 파일을 업로드
						if(file.getParentFile().mkdirs()) {
							file.createNewFile();
						}// mkdir
					}// exists
					
					// 임시로 생성(저장) MultipartFile을 실제 파일로 전송
					mFile.transferTo(new File(path + oFileName));
				}// getSize
			}
			logger.info(fileList.toString());
			
			return fileList;
		}
		// 파일 업로드 처리

		// 상품 전체 개수
		@Override
		public int getTotalCount() {
			return pdao.getTotalCount();
		}
}
