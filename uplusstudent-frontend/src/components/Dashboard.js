import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import AllCourses from './AllCourses';
import AvailableCourses from './AvailableCourses';
import MyCourses from './MyCourses';

const Dashboard = () => {
  const [activeTab, setActiveTab] = useState('available');
  const { user } = useAuth();

  const tabs = [
    { id: 'available', label: 'Available Courses', component: AvailableCourses },
    { id: 'enrolled', label: 'My Courses', component: MyCourses },
    { id: 'all', label: 'All Courses', component: AllCourses },
  ];

  const ActiveComponent = tabs.find(tab => tab.id === activeTab)?.component;

  return (
    <div className="container">
      <div className="card">
        <h2>Course Management Dashboard</h2>
        <p>Welcome back, {user?.full_name}! Manage your course enrollments below.</p>
      </div>

      <div className="tabs">
        {tabs.map(tab => (
          <button
            key={tab.id}
            className={`tab ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {ActiveComponent && <ActiveComponent />}
    </div>
  );
};

export default Dashboard;