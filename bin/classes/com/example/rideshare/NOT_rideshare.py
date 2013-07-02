import cgi
import urllib

from google.appengine.api import users
from google.appengine.ext import ndb
from datetime import datetime

import webapp2

REGUSERSECT = 'reg_user_section'

def reg_user_key(section_name=REGUSERSECT):
    return ndb.Key('Section', section_name)

class RegisteredUser(ndb.Model):
    """Models an individual RegisteredUser entry with name, phoneNumber, email, and password."""
    name = ndb.StringProperty(indexed=True)
    phoneNumber = ndb.StringProperty(indexed=False)
    email = ndb.StringProperty(indexed=True)
    password = ndb.StringProperty(indexed=False)
	
	def toDict(self):
		registereduser = {}
		registereduser["name"] = self.name
		registereduser["phoneNumber"] = self.phoneNumber
		registereduser["email"] = self.email
		registereduser["password"] = self.password
		return registereduser
                
##################################
class Login(webapp2.RequestHandler):

    def get(self):
        section_name = REGUSERSECT

		#Check if user actually exists
        #users_query = RegisteredUser.query(
        #    ancestor=reg_user_key(section_name))
        #users = users_query.filter('email =', self.request.get('email')) 	#should only return 1 user
		users = RegisteredUser.all().filter('email',self.request.get('email')).get()
		
        if users.email == self.request.get('email'):							#if email is a match
			if users.password == self.request.get('password'):				#if password is a match
				self.response.out.write('correct-' + user.name)
			else:															
				self.response.out.write('correct-') #CHANGE back to INCORRECT
                
        else: 																#if email is not a match 
				self.response.out.write('correct-') #CHANGE back to INCORRECT
				
###################################				
class RegUsersSection(webapp2.RequestHandler):

    def post(self):
	
        section_name = REGUSERSECT
        regsUser = RegisteredUser(parent=reg_user_key(section_name))
        
        #Check if user already exists
        #users_query = RegisteredUser.query(
        #    ancestor=reg_user_key(section_name))
        #users = users_query.filter("email =", self.request.get('email'))
		users = RegisteredUser.all().filter('email',self.request.get('email')).get()
        if users:    ##user exists
            self.response.out.write('User Already exists')
        
        else:                  ##add user to section
            regsUser.name = self.request.get('name')
            regsUser.email = self.request.get('email')
            regsUser.password = self.request.get('password')
            regsUser.phoneNumber = self.request.get('phoneNumber')
            greeting.put()


##################################
REGSEARCHSECT = 'ride_search_section'

def ride_offer_key(section_name=REGSEARCHSECT):
    return ndb.Key('Section', section_name)

class RideRequest(ndb.Model):
    name = ndb.StringProperty(indexed=True);
    email = ndb.StringProperty(indexed=True);
    location = ndb.StringProperty(indexed=True)
    destination = ndb.StringProperty(indexed=True)
    time_t = ndb.DateTimeProperty()

class SearchRider(webapp2.RequestHandler):

    def get(self):
        section_name = REGSEARCHSECT

        users_query = RideRequest.query(
            ancestor=ride_offer_key(section_name)).order(-RideRequest.name)
        users = users_query.filter('location',self.request.get('location')).filter('destination =', self.request.get('destination')).filter('time_t.month =', self.request.get('month')).filter('time_t.day =', self.request.get('day')).filter('time_t.year =', self.request.get('year')).filter('time_t.hour >=', self.request.get('from_hr')).filter('time_t.min >=',self.request.get('from_min')).filter('time_t.hour <=', self.request.get('to_hr')).filter('time_t.min <=',self.request.get('to_min'))

        if users:					#for all matches, add to response.out
            for user in users:
				self.response.out.write(users.name + '-' + users.email + '-')     #in android class keep track of name's position in list to user here
            
        else:
			self.response.out.write('No matches')
        
##############################
class RequestRide(webapp2.RequestHandler):

    def post(self):
	
        section_name = REGSEARCHSECT
		
        #LATER::::disallow users to input same request multiple times..
        rider = RideRequest(parent=ride_offer_key(section_name))
        rider.name = self.request.get('name')
        rider.email = self.request.get('email')
        rider.location = self.request.get('location')
        rider.destination = self.request.get('destination')
        rider.time_t.hr = self.request.get('hour'); 
        rider.time_t.min = self.request.get('minute')
        rider.time_t.month = self.request.get('month')
        rider.time_t.day = self.request.get('day')
        rider.time_t.year = self.request.get('year')
        rider.put()
        self.response.out.write('okay')



application = webapp2.WSGIApplication([
	('/', Login),
	('/register', RegUsersSection),
    ('/searchRider', SearchRider),
    ('/requestRide', RequestRide),
], debug=True)   #remember to change this




