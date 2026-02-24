#!/usr/bin/env python3
"""
Agent测试脚本（Python版本）
用于测试Agent的各种功能
"""

import requests
import json
import time
from typing import Dict, Any

# 配置
BASE_URL = "http://localhost:8080"
AGENT_URL = "http://localhost:5001"

# 颜色输出
class Colors:
    GREEN = '\033[0;32m'
    RED = '\033[0;31m'
    YELLOW = '\033[1;33m'
    NC = '\033[0m'  # No Color


def print_header(text: str):
    """打印标题"""
    print("\n" + "=" * 60)
    print(text)
    print("=" * 60 + "\n")


def test_api(name: str, method: str, url: str, data: Dict[str, Any] = None):
    """测试API"""
    print(f"{Colors.YELLOW}测试: {name}{Colors.NC}")
    
    try:
        if method == "GET":
            response = requests.get(url, timeout=10)
        elif method == "POST":
            response = requests.post(url, json=data, timeout=10)
        elif method == "PUT":
            response = requests.put(url, json=data, timeout=10)
        elif method == "DELETE":
            response = requests.delete(url, timeout=10)
        else:
            print(f"{Colors.RED}✗ 不支持的HTTP方法: {method}{Colors.NC}\n")
            return
        
        if response.status_code == 200:
            print(f"{Colors.GREEN}✓ 成功{Colors.NC}")
            try:
                result = response.json()
                print(f"响应: {json.dumps(result, ensure_ascii=False, indent=2)}")
            except:
                print(f"响应: {response.text}")
        else:
            print(f"{Colors.RED}✗ 失败 (HTTP {response.status_code}){Colors.NC}")
            print(f"响应: {response.text}")
    
    except requests.exceptions.ConnectionError:
        print(f"{Colors.RED}✗ 连接失败 - 请确保服务已启动{Colors.NC}")
    except requests.exceptions.Timeout:
        print(f"{Colors.RED}✗ 请求超时{Colors.NC}")
    except Exception as e:
        print(f"{Colors.RED}✗ 错误: {str(e)}{Colors.NC}")
    
    print()


def main():
    """主函数"""
    print_header("Agent功能测试脚本")
    
    # 1. 测试Agent健康检查
    print("1. 测试Agent健康检查")
    test_api("Agent健康检查", "GET", f"{AGENT_URL}/health")
    
    # 2. 测试Agent信息获取
    print("2. 测试Agent信息获取")
    test_api("Agent信息", "GET", f"{AGENT_URL}/info")
    
    # 3. 测试Agent直接执行
    print("3. 测试Agent直接执行")
    test_api("Agent执行", "POST", f"{AGENT_URL}/execute", {
        "input": {
            "city": "北京"
        }
    })
    
    # 4. 测试注册Agent到平台
    print("4. 测试注册Agent到平台")
    test_api("注册Agent", "POST", f"{BASE_URL}/agent", {
        "agentName": "天气查询Agent",
        "agentCode": "weather_agent",
        "agentType": "PYTHON",
        "description": "查询指定城市的天气信息",
        "endpointUrl": "http://localhost:5001/execute",
        "endpointType": "HTTP",
        "authType": "NONE",
        "inputSchema": json.dumps({
            "type": "object",
            "properties": {
                "city": {
                    "type": "string",
                    "description": "城市名称"
                }
            },
            "required": ["city"]
        }),
        "outputSchema": json.dumps({
            "type": "object",
            "properties": {
                "temperature": {"type": "string"},
                "weather": {"type": "string"}
            }
        }),
        "capabilities": json.dumps(["天气查询", "实时天气"]),
        "timeout": 30,
        "retryTimes": 0,
        "maxConcurrent": 10,
        "status": 1,
        "version": "1.0.0",
        "category": "天气服务",
        "tags": "天气,查询,实时",
        "priority": 0
    })
    
    # 等待一下，确保注册完成
    time.sleep(1)
    
    # 5. 测试查询Agent列表
    print("5. 测试查询Agent列表")
    test_api("查询Agent列表", "GET", f"{BASE_URL}/agent/page?pageNum=1&pageSize=10")
    
    # 6. 测试通过平台执行Agent
    print("6. 测试通过平台执行Agent")
    test_api("平台执行Agent", "POST", f"{BASE_URL}/agent/execute", {
        "agentCode": "weather_agent",
        "input": {
            "city": "上海"
        },
        "sessionId": "test_session_001"
    })
    
    # 7. 测试获取执行历史
    print("7. 测试获取执行历史")
    test_api("获取执行历史", "GET", f"{BASE_URL}/agent/history?agentCode=weather_agent")
    
    # 8. 测试获取可用Agent列表
    print("8. 测试获取可用Agent列表")
    test_api("获取可用Agent", "GET", f"{BASE_URL}/agent/available")
    
    print_header("测试完成！")


if __name__ == "__main__":
    main()

