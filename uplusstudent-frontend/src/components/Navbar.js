import React from 'react';
import { NavLink } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const Navbar = () => {
  const { user, logout } = useAuth();

  const handleLogout = () => {
    logout();
  };

  return (
    <nav className="navbar">
      <div className="container">
        <div className="brand-row">
          <h1>Student Admin System</h1>
          <div className="nav-links">
            <NavLink
              to="/dashboard"
              className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
            >
              Dashboard
            </NavLink>
            <NavLink
              to="/ai-chat"
              className={({ isActive }) => 'nav-link' + (isActive ? ' active' : '')}
            >
              💬 AI Chat
            </NavLink>
          </div>
        </div>
        <div className="user-info">
          <span>Welcome, {user?.full_name || user?.username}</span>
          <span>|</span>
          <span>{user?.email}</span>
          <button
            onClick={handleLogout}
            className="btn btn-danger"
            style={{ marginLeft: '15px' }}
          >
            Logout
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
