"""
Agent HTTP服务器
提供HTTP接口供Java后端调用
"""

from flask import Flask, request, jsonify
from typing import Dict, Any
import logging
import time
from .base_agent import BaseAgent

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class AgentServer:
    """Agent HTTP服务器"""
    
    def __init__(self, agent: BaseAgent, host: str = "0.0.0.0", port: int = 5000):
        """
        初始化Agent服务器
        
        Args:
            agent: Agent实例
            host: 监听地址
            port: 监听端口
        """
        self.agent = agent
        self.host = host
        self.port = port
        self.app = Flask(__name__)
        self._setup_routes()
        logger.info(f"初始化Agent服务器: {agent.agent_name} on {host}:{port}")
    
    def _setup_routes(self):
        """设置路由"""
        
        @self.app.route('/health', methods=['GET'])
        def health():
            """健康检查"""
            return jsonify({
                "status": "healthy",
                "agent_code": self.agent.agent_code,
                "agent_name": self.agent.agent_name
            })
        
        @self.app.route('/info', methods=['GET'])
        def info():
            """获取Agent信息"""
            return jsonify({
                "agent_code": self.agent.agent_code,
                "agent_name": self.agent.agent_name,
                "description": self.agent.description,
                "input_schema": self.agent.get_input_schema(),
                "output_schema": self.agent.get_output_schema(),
                "capabilities": self.agent.get_capabilities(),
                "category": self.agent.get_category(),
                "tags": self.agent.get_tags(),
                "timeout": self.agent.get_timeout()
            })
        
        @self.app.route('/execute', methods=['POST'])
        def execute():
            """执行Agent"""
            start_time = time.time()
            
            try:
                # 获取请求数据
                data = request.get_json()
                if not data:
                    return jsonify({
                        "error": "请求体不能为空"
                    }), 400
                
                input_data = data.get('input', {})
                agent_code = data.get('agent_code')
                
                logger.info(f"收到执行请求: agent_code={agent_code}, input={input_data}")
                
                # 验证Agent编码
                if agent_code and agent_code != self.agent.agent_code:
                    return jsonify({
                        "error": f"Agent编码不匹配: 期望 {self.agent.agent_code}, 实际 {agent_code}"
                    }), 400
                
                # 验证输入
                if not self.agent.validate_input(input_data):
                    return jsonify({
                        "error": "输入数据验证失败"
                    }), 400
                
                # 执行前置处理
                self.agent.before_execute(input_data)
                
                # 执行Agent
                output_data = self.agent.execute(input_data)
                
                # 执行后置处理
                self.agent.after_execute(output_data)
                
                # 计算执行时间
                execution_time = int((time.time() - start_time) * 1000)
                
                logger.info(f"执行成功: output={output_data}, time={execution_time}ms")
                
                # 返回结果
                response = {
                    "status": "success",
                    "agent_code": self.agent.agent_code,
                    "execution_time": execution_time,
                    **output_data
                }
                
                return jsonify(response)
                
            except Exception as e:
                # 错误处理
                error_response = self.agent.on_error(e)
                execution_time = int((time.time() - start_time) * 1000)
                
                logger.error(f"执行失败: {str(e)}", exc_info=True)
                
                return jsonify({
                    "status": "error",
                    "agent_code": self.agent.agent_code,
                    "execution_time": execution_time,
                    **error_response
                }), 500
        
        @self.app.route('/config', methods=['POST'])
        def set_config():
            """设置配置"""
            try:
                config = request.get_json()
                self.agent.set_config(config)
                return jsonify({
                    "status": "success",
                    "message": "配置已更新"
                })
            except Exception as e:
                logger.error(f"设置配置失败: {str(e)}", exc_info=True)
                return jsonify({
                    "status": "error",
                    "error": str(e)
                }), 500
    
    def run(self, debug: bool = False):
        """
        启动服务器
        
        Args:
            debug: 是否开启调试模式
        """
        logger.info(f"启动Agent服务器: {self.agent.agent_name}")
        logger.info(f"监听地址: http://{self.host}:{self.port}")
        logger.info(f"健康检查: http://{self.host}:{self.port}/health")
        logger.info(f"Agent信息: http://{self.host}:{self.port}/info")
        logger.info(f"执行接口: http://{self.host}:{self.port}/execute")
        
        self.app.run(host=self.host, port=self.port, debug=debug)

