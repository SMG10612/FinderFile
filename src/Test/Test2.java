package Test;

import java.util.Scanner;

abstract class Cal{
	public abstract int add(int a, int b);
	public abstract int sub(int a, int b);
	public abstract int mul(int a, int b);
	public abstract int na(int a, int b);
}

class GoodCalc extends Cal{
	
	@Override
	public int add(int a, int b) {
		return a+b;
	}
	
	@Override
	public int sub(int a, int b) {
		return a-b;
	}
	
	@Override
	public int mul(int a, int b) {
		return a*b;
	}
	
	@Override
	public int na(int a, int b) {
		return a/b;
	}
}
	

public class Test2 {
	public static void main(String[] args) {
		GoodCalc c = new GoodCalc();
		Scanner stdin = new Scanner(System.in);
		System.out.print("두 정수 연산자 입력>>> ");
		int num1 = stdin.nextInt();
		String cal = stdin.next();
		int num2 = stdin.nextInt();
		
		switch(cal) {
		case "+" : 
			System.out.println(c.add(num1, num2)); break;
		case "-" : 
			System.out.println(c.sub(num1, num2)); break;
		case "*" : 
			System.out.println(c.mul(num1, num2)); break;
		case "/" : 
			System.out.println(c.na(num1, num2)); break;
		default : System.out.println("잘못 입력하셨습니다."); break;
		}
	}
}
