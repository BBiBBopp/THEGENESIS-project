package com.thegenesis.sweethome.house.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import com.google.gson.Gson;
import com.thegenesis.sweethome.common.template.saveFile;
import com.thegenesis.sweethome.house.model.service.HouseService;
import com.thegenesis.sweethome.house.model.vo.House;
import com.thegenesis.sweethome.house.model.vo.HouseFile;
import com.thegenesis.sweethome.member.model.vo.Member;
import com.thegenesis.sweethome.room.model.service.RoomService;
import com.thegenesis.sweethome.room.model.vo.Room;

@Controller
public class HouseController {
	
	@Autowired
	private HouseService houseService;
	
	@Autowired
	private RoomService roomService;
	
	// 테스트용
	@RequestMapping("houseInsert")
	public String houseInsert() {
		return "house/houseInsert";
	}
	
	@RequestMapping("test")
	public String test() {
		return "house/test";
	}
	
	/**
	 * 하우스 등록
	 */
	@RequestMapping("insertHouse.ho")
	public String insertHouse(House h, HouseFile hf, Room r, MultipartFile[] upfile, int[] fileNumber, HttpSession session) {
		
		// 로그인 유저 번호 확인
		if(session.getAttribute("loginUser") != null) {
			h.setUserNo(((Member)session.getAttribute("loginUser")).getUserNo());			
		} else {
			session.setAttribute("errorMsg", "로그인하시기 바랍니다.");
			return "redirect:/";
		}
		
		int resultHouse = 0; // 하우스 등록 결과
		int resultRoom = 0; // 방 등록 결과
		int resultHouseFile = 0; // 하우스 파일 등록 결과
		
		// 하우스 정보 textarea 줄바꿈 <br> 처리
		h.setHouseTitle(h.getHouseTitle().replace(System.lineSeparator(), "<br>"));
		h.setHouseIntroduce(h.getHouseIntroduce().replace(System.lineSeparator(), "<br>"));
		h.setShareSpace(h.getShareSpace().replace(System.lineSeparator(), "<br>"));
		h.setPersonalSpace(h.getPersonalSpace().replace(System.lineSeparator(), "<br>"));
		h.setTraffic(h.getTraffic().replace(System.lineSeparator(), "<br>"));
		h.setConvenience(h.getConvenience().replace(System.lineSeparator(), "<br>"));
		
		// 하우스 등록
		resultHouse = houseService.insertHouse(h);
		
		// 방 등록 성공한 방 번호 리스트
		ArrayList<Integer> roomNoArr = new ArrayList<>(); // 방 번호 리스트
		roomNoArr.add(0); // 대표이미지용
		
		// 하우스 등록 성공시 방 등록
		if(resultHouse > 0) {
			int roomNum = r.getRoomNameArr().length; // 입력된 방 개수
			int resultTempRoom = 0; // 임시 방 등록 결과
			
			// 순차적으로 방 입력
			for(int i = 0; i < roomNum; i ++) {
				Room tempRoom = Room.builder()
									.roomName(r.getRoomNameArr()[i])
									.gender(r.getGenderArr()[i])
									.gender(r.getGenderArr()[0])
									.people(r.getPeopleArr()[i])
									.area(r.getAreaArr()[i])
									.deposit(r.getDepositArr()[i])
									.rent(r.getRentArr()[i])
									.expense(r.getExpenseArr()[i])
									.utility(r.getUtilityArr()[i])
									.availableDate(r.getAvailableDateArr()[i])
									.build();
				
				// 임시 방 입력
				resultTempRoom = roomService.insertRoom(tempRoom);
				
				// 임시 방 등록 성공시 방 번호 리스트에 추가
				if(resultTempRoom > 0) {
					roomNoArr.add(roomService.selectRoomNo() - 1);
				} else {
					break;
				}
			}
			
			// 입력된 방 개수와 임시 방 등록 성공한 방 번호 리스트 일치 확인
			if(roomNoArr.size() == roomNum + 1) {
				resultRoom = 1;
			}
			
			// 방 등록 성공시 하우스 파일 등록
			if(resultRoom > 0) {
				int fileCheck = 0; // 하우스 파일 순서 확인
				int fileLevel = 1; // 이미지 종류 확인
				ArrayList<HouseFile> hfList = new ArrayList<>(); // 하우스 파일 등록용 ArrayList
				
				// 하우스 이미지 파일 저장
				for(int i = 0; i < fileNumber.length; i++) { // i = 방 번호 확인
					for(int j = 0; j < fileNumber[i]; j++) { // j = 파일 개수 확인
						
						if(i > 0) { // 대표이미지가 아닐 경우
							fileLevel = 2;
						}
						
						// 서버 업로드용 파일명 변경
						String changeName = saveFile.changeFileName(upfile[fileCheck], session);
						
						// 해당 하우스 이미지 파일 리스트로 저장
						hfList.add(HouseFile.builder()
											.roomNo(roomNoArr.get(i))
											.originName(upfile[fileCheck].getOriginalFilename())
											.changeName(changeName)
											.filePath("resources/uploadFiles/" + changeName)
											.fileLevel(fileLevel)
											.build());
						
						fileCheck++;
					}
				}
				
				// 하우스 파일 등록
				resultHouseFile = houseService.insertHouseFile(hfList);
			}
		}
		
		if(resultHouse * resultRoom * resultHouseFile > 0) {
			session.setAttribute("alertMsg", "하우스가 정상 등록되었습니다.");
			return "redirect:/";
		} else {
			session.setAttribute("errorMsg", "하우스 등록에 실패하였습니다.");
			return "redirect:/";
		}
		
	}
	
	/**
	 * 하우스 삭제
	 * @param session
	 * @param hno
	 * @return
	 */
	@RequestMapping("deleteHouse.ho")
	public String deleteHouse(HttpSession session, int hno) {
		
		int userNo = 0; // 하우스 유저 번호
		int resultHouse = 0; // 하우스 등록 결과
		int resultRoom = 0; // 방 등록 결과
		int resultHouseFile = 0; // 하우스 파일 등록 결과
		
		// 로그인 유저 번호 확인
		if(session.getAttribute("loginUser") != null) {
			userNo = ((Member)session.getAttribute("loginUser")).getUserNo();			
		} else {
			session.setAttribute("errorMsg", "로그인하시기 바랍니다.");
			return "redirect:/";
		}
		
		// 유저 정보 입력
		HashMap<String, Integer> userInfo = new HashMap<>();
		
		userInfo.put("userNo", userNo);
		userInfo.put("houseNo", hno);
		
		System.out.println("userInfo: " + userInfo);
		
		// 하우스 삭제
		resultHouse = houseService.deleteHouse(userInfo);
		
		System.out.println("하우스 삭제 성공");
		
		if(resultHouse > 0) { // 하우스 삭제 성공 시 해당 하우스 방 모두 삭제
			// 방 삭제
			resultRoom = roomService.deleteRoom(hno);
			
			System.out.println("방 삭제 성공");
			
			if(resultRoom > 0) { // 방 모두 삭제 성공 시 하우스 모든 이미지 삭제
				// 하우스의 모든 이미지 파일 경로 가져오기
				ArrayList<HouseFile> hfList = houseService.selectHouseFile(hno);
				
				System.out.println("모든 이미지 파일 경로 가져오기 성공");
				
				// 서버에 저장된 실제 이미지 삭제
				for(HouseFile hf: hfList) {
					new File(session.getServletContext().getRealPath(hf.getFilePath())).delete();
					System.out.println("삭제 성공: " + hf.getFilePath());
				}
				
				// 실제 이미지 삭제 후 DB 삭제
				resultHouseFile = houseService.deleteHouseFile(hno);
				
				System.out.println("하우스 파일 삭제 성공");
			}
		}
		
		if(resultHouse * resultRoom * resultHouseFile > 0) {
			session.setAttribute("alertMsg", "하우스가 정상 삭제되었습니다.");
			return "redirect:/";			
		} else {
			session.setAttribute("errorMsg", "하우스 삭제에 실패하였습니다.");
			return "redirect:/";
		}
		
	}
	
	@RequestMapping("house.se")
	public ModelAndView houseSearch(ModelAndView mv) {
		
	
		ArrayList<House> list = houseService.houseSearch();
		
		
		ArrayList<House> list1= houseService.houseSearchOne();

		ArrayList<HouseFile> list2 = houseService.houseSearchTwo();
		
		
		mv.addObject("list", list).addObject("list1", list1).addObject("list2",list2).setViewName("house/houseList");
		
		
		
		
		return mv;
	}
	
	
	
	@ResponseBody
	@RequestMapping(value = "keyword.se", produces="application/json; charset=utf-8")
	public String keywordSearch(String keyword) {
		
		ArrayList<House> list1 = houseService.keywordSearch(keyword);
		
		
		return new Gson().toJson(list1);
	}

	
	
}
