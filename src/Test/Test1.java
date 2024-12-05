/*add,sub,mul,div 클래스 생성
 * 각 클래스의 공통 필드 메소드
 * -int a, b  :2개의 피연산자
 * -void serValud(int a, int b):피연산자 값 객체 내에 저장
 * -int calculate():클래스 목적에 맞는 연산 실행 후 결과 return
 * main()메소드
 * -add,sub,mul,div 객체 생성
 * -setValue,calculate() 호출 후 결과 출력
 * 실행결과
 * -두 정수 연산자 입력 >> 5 7 *
 * 	35
 * */
package Test;

import java.util.Scanner;


class add{
	int a , b;
	void setValue(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	int calculate() {
		return a+b;
	}
}
class sub{
	int a , b;
	void setValue(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	int calculate() {
		return a-b;
	}
}
class mul{
	int a , b;
	void setValue(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	int calculate() {
		return a*b;
	}
}
class div{
	int a , b;
	void setValue(int a, int b) {
		this.a = a;
		this.b = b;
	}
	
	int calculate() {
		return a/b;
	}
}
public class Test1 {
	public static void main(String[] args) {
		Scanner stdin = new Scanner(System.in);
		add add = new add();
		sub sub = new sub();
		mul mul = new mul();
		div div = new div();
		
		System.out.print("두 정수 연산자 입력 >> ");
		int num1 = stdin.nextInt();
		int num2 = stdin.nextInt();
		String cal = stdin.next();
		
		switch(cal) {
			case "+" : 
				add.setValue(num1, num2);
				System.out.println(add.calculate());
				break;
			case "-" : 
				sub.setValue(num1, num2);
				System.out.println(sub.calculate());
				break;
			case "*" : 
				mul.setValue(num1, num2);
				System.out.println(mul.calculate());
				break;
			case "/" : 
				div.setValue(num1, num2);
				System.out.println(div.calculate());	
				break;
			default : System.out.println("잘못 입력하셨습니다.");
		}
		
	}
}
