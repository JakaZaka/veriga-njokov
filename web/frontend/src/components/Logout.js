import { useEffect, useContext } from 'react';
import { UserContext } from '../userContext';
import { Navigate } from 'react-router-dom';

function Logout() {
    const userContext = useContext(UserContext); 
    useEffect(function() {
        userContext.setUserContext(null);
        localStorage.removeItem('token');
    }, []);

    return (
        <Navigate replace to="/" />
    );
}

export default Logout;