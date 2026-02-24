"""
Agent基类
所有自定义Agent都应该继承此类
"""

from abc import ABC, abstractmethod
from typing import Dict, Any, List, Optional
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


class BaseAgent(ABC):
    """Agent基类"""
    
    def __init__(self, agent_code: str, agent_name: str, description: str):
        """
        初始化Agent
        
        Args:
            agent_code: Agent唯一编码
            agent_name: Agent名称
            description: Agent描述
        """
        self.agent_code = agent_code
        self.agent_name = agent_name
        self.description = description
        self.config = {}
        logger.info(f"初始化Agent: {agent_name} ({agent_code})")
    
    @abstractmethod
    def execute(self, input_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        执行Agent逻辑（子类必须实现）
        
        Args:
            input_data: 输入数据字典
            
        Returns:
            输出数据字典
        """
        pass
    
    def get_input_schema(self) -> Dict[str, Any]:
        """
        获取输入参数Schema（JSON Schema格式）
        子类可以重写此方法来定义输入参数
        
        Returns:
            JSON Schema字典
        """
        return {
            "type": "object",
            "properties": {},
            "required": []
        }
    
    def get_output_schema(self) -> Dict[str, Any]:
        """
        获取输出结果Schema（JSON Schema格式）
        子类可以重写此方法来定义输出格式
        
        Returns:
            JSON Schema字典
        """
        return {
            "type": "object",
            "properties": {}
        }
    
    def get_capabilities(self) -> List[str]:
        """
        获取Agent能力列表
        子类可以重写此方法来定义能力
        
        Returns:
            能力列表
        """
        return []
    
    def get_category(self) -> str:
        """
        获取Agent分类
        子类可以重写此方法来定义分类
        
        Returns:
            分类名称
        """
        return "通用"
    
    def get_tags(self) -> List[str]:
        """
        获取Agent标签
        子类可以重写此方法来定义标签
        
        Returns:
            标签列表
        """
        return []
    
    def get_timeout(self) -> int:
        """
        获取超时时间（秒）
        子类可以重写此方法来定义超时时间
        
        Returns:
            超时时间（秒）
        """
        return 30
    
    def set_config(self, config: Dict[str, Any]):
        """
        设置配置参数
        
        Args:
            config: 配置字典
        """
        self.config = config
        logger.info(f"设置配置: {config}")
    
    def validate_input(self, input_data: Dict[str, Any]) -> bool:
        """
        验证输入数据（可选）
        子类可以重写此方法来实现自定义验证
        
        Args:
            input_data: 输入数据
            
        Returns:
            是否验证通过
        """
        return True
    
    def before_execute(self, input_data: Dict[str, Any]):
        """
        执行前的钩子方法
        子类可以重写此方法来实现前置处理
        
        Args:
            input_data: 输入数据
        """
        pass
    
    def after_execute(self, output_data: Dict[str, Any]):
        """
        执行后的钩子方法
        子类可以重写此方法来实现后置处理
        
        Args:
            output_data: 输出数据
        """
        pass
    
    def on_error(self, error: Exception) -> Dict[str, Any]:
        """
        错误处理钩子方法
        子类可以重写此方法来实现自定义错误处理
        
        Args:
            error: 异常对象
            
        Returns:
            错误响应字典
        """
        logger.error(f"Agent执行错误: {str(error)}", exc_info=True)
        return {
            "error": str(error),
            "error_type": type(error).__name__
        }

