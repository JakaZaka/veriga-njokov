import React, { useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import Register from './components/Register';
import Login from './components/Login';
import Logout from './components/Logout';
import Profile from './components/Profile';
import { UserContext } from './userContext';

function App() {
  const [user, setUserContext] = useState(null);

  return (
    <UserContext.Provider value={{ user, setUserContext }}>
      <Router>
        <nav>
          <Link to="/register">Register</Link> |{' '}
          <Link to="/login">Login</Link> |{' '}
          {user && (
            <>
              <Link to="/logout">Logout</Link> |{' '}
              <Link to="/profile">Profile</Link>
            </>
          )}
        </nav>
        <Routes>
          <Route path="/register" element={<Register />} />
          <Route path="/login" element={<Login />} />
          <Route path="/logout" element={<Logout />} />
          <Route path="/profile" element={<Profile />} />
        </Routes>
      </Router>
    </UserContext.Provider>
  );
}

export default App;