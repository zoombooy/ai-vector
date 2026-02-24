#!/bin/bash

# Agent测试脚本
# 用于测试Agent的各种功能

echo "=========================================="
echo "Agent功能测试脚本"
echo "=========================================="
echo ""

# 颜色定义
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 基础URL
BASE_URL="http://localhost:8080"
AGENT_URL="http://localhost:5001"

# 测试函数
test_api() {
    local name=$1
    local method=$2
    local url=$3
    local data=$4
    
    echo -e "${YELLOW}测试: $name${NC}"
    
    if [ "$method" == "GET" ]; then
        response=$(curl -s -w "\n%{http_code}" "$url")
    else
        response=$(curl -s -w "\n%{http_code}" -X "$method" "$url" \
            -H "Content-Type: application/json" \
            -d "$data")
    fi
    
    http_code=$(echo "$response" | tail -n1)
    body=$(echo "$response" | sed '$d')
    
    if [ "$http_code" -eq 200 ]; then
        echo -e "${GREEN}✓ 成功${NC}"
        echo "响应: $body"
    else
        echo -e "${RED}✗ 失败 (HTTP $http_code)${NC}"
        echo "响应: $body"
    fi
    echo ""
}

# 1. 测试Agent健康检查
echo "1. 测试Agent健康检查"
test_api "Agent健康检查" "GET" "$AGENT_URL/health"

# 2. 测试Agent信息获取
echo "2. 测试Agent信息获取"
test_api "Agent信息" "GET" "$AGENT_URL/info"

# 3. 测试Agent直接执行
echo "3. 测试Agent直接执行"
test_api "Agent执行" "POST" "$AGENT_URL/execute" \
'{
  "input": {
    "city": "北京"
  }
}'

# 4. 测试注册Agent到平台
echo "4. 测试注册Agent到平台"
test_api "注册Agent" "POST" "$BASE_URL/agent" \
'{
  "agentName": "天气查询Agent",
  "agentCode": "weather_agent",
  "agentType": "PYTHON",
  "description": "查询指定城市的天气信息",
  "endpointUrl": "http://localhost:5001/execute",
  "endpointType": "HTTP",
  "authType": "NONE",
  "inputSchema": "{\"type\":\"object\",\"properties\":{\"city\":{\"type\":\"string\",\"description\":\"城市名称\"}},\"required\":[\"city\"]}",
  "outputSchema": "{\"type\":\"object\",\"properties\":{\"temperature\":{\"type\":\"string\"},\"weather\":{\"type\":\"string\"}}}",
  "capabilities": "[\"天气查询\",\"实时天气\"]",
  "timeout": 30,
  "retryTimes": 0,
  "maxConcurrent": 10,
  "status": 1,
  "version": "1.0.0",
  "category": "天气服务",
  "tags": "天气,查询,实时",
  "priority": 0
}'

# 5. 测试查询Agent列表
echo "5. 测试查询Agent列表"
test_api "查询Agent列表" "GET" "$BASE_URL/agent/page?pageNum=1&pageSize=10"

# 6. 测试通过平台执行Agent
echo "6. 测试通过平台执行Agent"
test_api "平台执行Agent" "POST" "$BASE_URL/agent/execute" \
'{
  "agentCode": "weather_agent",
  "input": {
    "city": "上海"
  },
  "sessionId": "test_session_001"
}'

# 7. 测试获取执行历史
echo "7. 测试获取执行历史"
test_api "获取执行历史" "GET" "$BASE_URL/agent/history?agentCode=weather_agent"

# 8. 测试获取可用Agent列表
echo "8. 测试获取可用Agent列表"
test_api "获取可用Agent" "GET" "$BASE_URL/agent/available"

echo "=========================================="
echo "测试完成！"
echo "=========================================="

