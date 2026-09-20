#!/bin/bash

echo "=== Student Admin System Frontend Setup Check ==="
echo ""

echo "✓ Checking project structure..."
if [ -f "package.json" ]; then
    echo "  ✓ package.json exists"
else
    echo "  ✗ package.json missing"
fi

if [ -d "src" ]; then
    echo "  ✓ src directory exists"
else
    echo "  ✗ src directory missing"
fi

if [ -d "public" ]; then
    echo "  ✓ public directory exists"
else
    echo "  ✗ public directory missing"
fi

echo ""
echo "✓ Checking React components..."
components=("Login.js" "Dashboard.js" "Navbar.js" "CourseCard.js" "AllCourses.js" "AvailableCourses.js" "MyCourses.js")
for component in "${components[@]}"; do
    if [ -f "src/components/$component" ]; then
        echo "  ✓ $component exists"
    else
        echo "  ✗ $component missing"
    fi
done

echo ""
echo "✓ Checking core files..."
core_files=("src/App.js" "src/index.js" "src/index.css" "src/context/AuthContext.js" "src/services/api.js")
for file in "${core_files[@]}"; do
    if [ -f "$file" ]; then
        echo "  ✓ $file exists"
    else
        echo "  ✗ $file missing"
    fi
done

echo ""
echo "=== Setup Instructions ==="
echo "1. Install Node.js if not already installed"
echo "2. Run: npm install"
echo "3. Start your Spring Boot backend on http://localhost:8080"
echo "4. Run: npm start"
echo "5. Open http://localhost:3000 in your browser"
echo "6. Login with username: john_student, password: password"
echo ""
echo "=== Backend API Test ==="
echo "Testing if Spring Boot backend is running..."
if curl -s http://localhost:8080/actuator/health > /dev/null; then
    echo "✓ Backend is running and accessible"
else
    echo "✗ Backend is not accessible. Make sure it's running on http://localhost:8080"
fi