-- 插入地图MCP Server
INSERT INTO `t_mcp_server` (
    `server_name`,
    `server_code`,
    `description`,
    `mcp_url`,
    `server_type`,
    `auth_type`,
    `source`,
    `source_url`,
    `category`,
    `tags`,
    `priority`,
    `status`,
    `timeout`,
    `health_status`
) VALUES (
    '地图服务',
    'map_service',
    'MCP Market提供的地图服务工具，支持地理位置查询、地图搜索等功能',
    'https://mcpmarket.cn/mcp/ec85ce5c9b65a738a3339c4e',
    'HTTP_STREAM',
    'NONE',
    'MCPMARKET',
    'https://mcpmarket.cn/mcp/ec85ce5c9b65a738a3339c4e',
    'api',
    '地图,位置,搜索,导航',
    10,
    1,
    30000,
    'UNKNOWN'
);

-- 查询验证
SELECT * FROM t_mcp_server;
SELECT * FROM t_mcp_tool;

