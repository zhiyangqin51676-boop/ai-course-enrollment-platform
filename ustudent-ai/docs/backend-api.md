# ustudent 后端 API 速查（给 AI 服务消费）

> 这是 **AI 服务调用后端时唯一的真相来源**。教案里偶尔出现的简化路径（如 `/courses/{course_code}`）是黑板示意，**实际后端 ID 都是数字**，需要先 `GET /api/courses` 再按 `course_code` 字符串本地过滤。
>
> JSON 字段命名：`SNAKE_CASE`（`course_code`、`current_enrollments`、`full_name`…），不是 camelCase。

## Base URL

- 本地直接跑后端：`http://localhost:8080`
- docker-compose 内：`http://ustudent-backend:8080`
- AWS 上：见 `infrastructure/terraform` 的 ALB 输出

## 端点清单

### 健康
| 方法 | 路径 | 说明 |
|---|---|---|
| GET | `/actuator/health` | Spring 标准健康端点 |

### 认证 (`AuthController`)
| 方法 | 路径 | 入参 | 说明 |
|---|---|---|---|
| POST | `/api/login` | body `{username, password}` | 返回 `{token, user}`（如有 JWT） |

### 课程 (`CourseController`)
| 方法 | 路径 | 入参 | 说明 |
|---|---|---|---|
| GET | `/api/courses` | `?studentId=<long>` 可选 | 列所有课程；带 studentId 时只返可选 |

返回 `ApiResponse<List<CourseDto>>`，`CourseDto` 字段：
`id, course_code, course_name, description, credits, max_students, current_enrollments, teacher{ id, full_name, email }`

### 选课 (`EnrollmentController`)
| 方法 | 路径 | 入参 | 说明 |
|---|---|---|---|
| POST | `/api/courses/{courseId}/enroll` | `?studentId=<long>` | 给学生选课 |
| GET | `/api/me/courses` | `?studentId=<long>` | 学生已选 |
| DELETE | `/api/courses/{courseId}/drop` | `?studentId=<long>` | 退课 |

### 先修课 (`PrerequisiteController`)
| 方法 | 路径 | 入参 |
|---|---|---|
| GET | `/api/courses/{courseId}/eligibility` | `?studentId=<long>` |
| GET | `/api/students/{studentId}/eligible-courses` | - |

### 课表 (`ScheduleController`)
| 方法 | 路径 | 入参 |
|---|---|---|
| GET | `/api/courses/{courseId}/conflicts` | `?studentId=<long>` |
| GET | `/api/students/{studentId}/schedule` | - |

### 学生档案 (`StudentProfileController`)
| 方法 | 路径 |
|---|---|
| GET | `/api/students/{studentId}/profile` |

## Agent 工具与端点的映射（给第 9 课用）

| Agent 工具 | 用到的后端调用 |
|---|---|
| `list_courses(student_id)` | `GET /api/courses?studentId={student_id}` |
| `get_course(course_code)` | `GET /api/courses` → 在结果中按 `course_code` 过滤 → 取第一个 |
| `enroll(student_id, course_code)` | 先用 `get_course` 拿 `id` → `POST /api/courses/{id}/enroll?studentId={student_id}` |
| `drop(student_id, course_code)` | 先用 `get_course` 拿 `id` → `DELETE /api/courses/{id}/drop?studentId={student_id}` |
| `my_courses(student_id)` | `GET /api/me/courses?studentId={student_id}` |
| `check_eligibility(student_id, course_code)` | 先映射 ID → `GET /api/courses/{id}/eligibility?studentId={student_id}` |

> **可选优化**：若后端可改，加一个 `GET /api/courses/by-code/{code}` 端点，AI 这边就不用每次取全量再过滤。阶段 C 决定是否提 PR 给后端。
