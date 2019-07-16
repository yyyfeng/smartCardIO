package com.yingf;
 
import java.util.List;
 
import javax.smartcardio.Card;
import javax.smartcardio.CardChannel;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CommandAPDU;
import javax.smartcardio.ResponseAPDU;
import javax.smartcardio.TerminalFactory;
 
/**
 * 描 述: <描述>.acr.java
 * 
 * @author guolp
 * @since
 */
public class acr {
 
	/**
	 * <一句话描述该方法的功能>
	 * 
	 * @param args
	 * @since
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		TerminalFactory factory = TerminalFactory.getDefault();// 得到一个默认的读卡器工厂(迷。。)
		List<CardTerminal> terminals;// 创建一个List用来放读卡器(谁没事会在电脑上插三四个读卡器。。)
		try {
			terminals = factory.terminals().list();// 从工厂获得插在电脑上的读卡器列表,get读卡器列表
			terminals.stream().forEach(
					s -> System.out.println(s)
			);// 打印获取到的读卡器名称
            System.out.println("读卡器的个数为："+terminals.size());;
 
			CardTerminal a = terminals.get(0);// 使用第0个读卡器[暂且不考虑同时插N个读卡器的情况了]
			a.waitForCardPresent(0L);// 等待放置卡片
			Card card = a.connect("T=1");// 连接卡片，协议T=1 块读写(T=0貌似不支持，一用就报错)
			CardChannel channel = card.getBasicChannel();// 打开通道

			CommandAPDU getUID = new CommandAPDU(0xFF, 0xCA, 0x00, 0x00, 0x04);// 中文API第12页
			ResponseAPDU r = channel.transmit(getUID);// 发送getUID指令
			System.out.println("UID: " + r.toString());// 返回:UID: ResponseAPDU: 6 bytes, SW=9000
			System.out.println("Data:" + bytesToHexString(r.getData()));
			// 即返回卡号，操作成功
 
			// 加载认证密钥，放在读卡器的EEPROM-->指带电可擦可编程只读存储器。是一种掉电后数据不丢失的存储芯片。 EEPROM 可以在电脑上或专用设备上擦除已有信息，重新编程。一般用在即插即用。
			byte[] pwd = { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };// 先用一个数组把密钥存起来
			CommandAPDU loadPWD = new CommandAPDU(0xFF, 0x82, 0x00, 0x00, pwd, 0, 6);// 构造加载认证密钥APDU指令 ，中文API第13页
			ResponseAPDU r1 = channel.transmit(loadPWD);// 发送loadPWD指令
			System.out.println("加载认证密钥: " + r1.toString());
 
			// 认证密钥，验证通过后，读取同一扇区的其他块不需要再次验证，中文API原文
			byte[] check = { (byte) 0x01, (byte) 0x00, (byte) 0x0B, (byte) 0x60, (byte) 0x00 };
			// 0x01认证版本，0x00空闲，0x08认证区块号，0x60密钥类型A/0x61密钥类型B,0x00密钥存储的地址(密钥号)
			CommandAPDU authPWD = new CommandAPDU(0xFF, 0x86, 0x00, 0x00, check, 0, 5);// 加上指令头部，构造出完整的认证APDU指令，中文API第14页
			ResponseAPDU r2 = channel.transmit(authPWD);// 发送 认证指令
			System.out.println("认证第8区块：" + r2.toString());
 
			// 读区块2
			CommandAPDU getData8 = new CommandAPDU(0xFF, 0xB0, 0x00, 0x08, 0x10);// 构造 读区块APDU指令，中文API第17页
			ResponseAPDU r8 = channel.transmit(getData8);// 发送 读区块指令
			System.out.println("第8个区块,Data:" + bytesToHexString(r8.getData()));
 
			CommandAPDU getData9 = new CommandAPDU(0xFF, 0xB0, 0x00, 0x09, 0x10);// 构造 读区块APDU指令
			ResponseAPDU r9 = channel.transmit(getData9);// 发送 读区块指令
			System.out.println("第9个区块,Data:" + bytesToHexString(r9.getData()));
 
			CommandAPDU getData10 = new CommandAPDU(0xFF, 0xB0, 0x00, 0x0A, 0x10);// 构造 读区块APDU指令
			ResponseAPDU r10 = channel.transmit(getData10);// 发送 读区块指令
			System.out.println("第10个区块,Data:" + bytesToHexString(r10.getData()));
 
			CommandAPDU getData11 = new CommandAPDU(0xFF, 0xB0, 0x00, 0x0B, 0x10);// 构造 读区块APDU指令
			ResponseAPDU r11 = channel.transmit(getData11);// 发送 读区块指令
			System.out.println("第11个区块,Data:" + bytesToHexString(r11.getData()));
 
			// 写区块
			byte[] up = { (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03, (byte) 0x04, (byte) 0x05, (byte) 0x06,
					(byte) 0x07, (byte) 0x08, (byte) 0x09, (byte) 0x0A, (byte) 0x0B, (byte) 0x0C, (byte) 0x0D,
					(byte) 0x0E, (byte) 0x0F };// 0x 构造要写入的数据，有16位
			CommandAPDU upData = new CommandAPDU(0xFF, 0xD6, 0x00, 0x08, up, 0, 16);//构造 写区块APDU指令，中文API第18页
			ResponseAPDU r4 = channel.transmit(upData);// 发送写块指令
			System.out.println("写区块: " + r4.toString());
			System.out.println("写区块: " + bytesToHexString(r4.getData()) + ":#:" + bytesToHexString(r4.getBytes()));// 打印返回值
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
 
	/**
	 * <一句话描述该方法的功能>
	 * 
	 * @param data
	 * @return
	 * @author guolp
	 * @since 1.0, 2018-10-24 09:28:48
	 */
	private static final char[] HEX_CHAR = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
			'f' };
 
	private static String bytesToHexString(byte[] bytes) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		int a = 0;
		for (byte b : bytes) { // 使用除与取余进行转换
			if (b < 0) {
				a = 256 + b;
			} else {
				a = b;
			}
			// sb.append("0x");
			sb.append(HEX_CHAR[a / 16]);
			sb.append(HEX_CHAR[a % 16]);
			// sb.append(" ");
		}
		return sb.toString().toUpperCase();
	}
 
}
