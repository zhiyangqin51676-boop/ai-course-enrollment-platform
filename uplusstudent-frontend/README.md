# Student Admin System - Frontend

A React-based frontend application for the Student Admin System that allows students to browse courses, enroll in available courses, and manage their enrollments.

## Features

- **User Authentication**: Secure login system
- **Course Browsing**: View all courses in the system
- **Course Enrollment**: Enroll in available courses
- **My Courses**: View enrolled courses and enrollment status
- **Responsive Design**: Works on desktop and mobile devices

## Prerequisites

- Node.js (version 14 or higher)
- npm or yarn
- Running Spring Boot backend on `http://localhost:8080`

## Installation

1. Clone the repository and navigate to the project directory:
```bash
cd uplusstudent-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm start
```

The application will open in your browser at `http://localhost:3000`.

## Usage

### Login
- Use the demo credentials:
  - **Username**: `john_student`
  - **Password**: `password`

### Dashboard Features

1. **Available Courses Tab**
   - View courses you can enroll in
   - See course details including teacher, credits, and enrollment status
   - Enroll in courses with available spots

2. **My Courses Tab**
   - View your enrolled courses
   - See enrollment date and status
   - Track total credits

3. **All Courses Tab**
   - Browse all courses in the system
   - View course information and enrollment statistics

## API Integration

The frontend integrates with the following Spring Boot API endpoints:

- `POST /api/login` - User authentication
- `GET /api/courses` - Get all courses
- `GET /api/courses?studentId={id}` - Get available courses for student
- `POST /api/courses/{id}/enroll?studentId={id}` - Enroll in course
- `GET /api/me/courses?studentId={id}` - Get student's enrollments

## Project Structure

```
src/
├── components/          # React components
│   ├── Login.js        # Login form
│   ├── Dashboard.js    # Main dashboard
│   ├── Navbar.js       # Navigation bar
│   ├── CourseCard.js   # Reusable course card
│   ├── AllCourses.js   # All courses view
│   ├── AvailableCourses.js # Available courses view
│   └── MyCourses.js    # Enrolled courses view
├── context/            # React context
│   └── AuthContext.js  # Authentication context
├── services/           # API services
│   └── api.js         # API client and endpoints
├── App.js             # Main app component
├── index.js           # App entry point
└── index.css          # Global styles
```

## Available Scripts

- `npm start` - Start development server
- `npm build` - Build for production
- `npm test` - Run tests
- `npm eject` - Eject from Create React App

## Technologies Used

- **React 18** - Frontend framework
- **React Router** - Client-side routing
- **Axios** - HTTP client for API calls
- **Context API** - State management
- **CSS3** - Styling and responsive design

## Configuration

The application is configured to proxy API requests to `http://localhost:8080` during development. This is set in the `package.json` file:

```json
"proxy": "http://localhost:8080"
```

For production deployment, update the API base URL in `src/services/api.js`.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is licensed under the MIT License.