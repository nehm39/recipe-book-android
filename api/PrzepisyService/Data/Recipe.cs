using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.Serialization;
using System.Web;
using System.Web.Script.Serialization;

namespace PrzepisyService.Data
{
    [DataContract]
    public class Recipe
    {
        [DataMember(Order = 0)]
        public int Id { get; set; }

        [DataMember(Order = 1)]
        public string Name { get; set; }

        [DataMember(Order = 2)]
        public string Description { get; set; }

        [DataMember(Order = 3)]
        public List<int> Categories { get; set; }

        [DataMember(Order = 4)]
        public string Photo { get; set; }

        [DataMember(Order = 5)]
        public string ModificationDate { get; set; }

        [DataMember(Order = 6)]
        public char Modifier { get; set; }

        [DataMember(Order = 7)]
        public int Version { get; set; }

        protected bool Equals(Recipe other)
        {
            return Id == other.Id;
        }

        public override bool Equals(object obj)
        {
            if (ReferenceEquals(null, obj)) return false;
            if (ReferenceEquals(this, obj)) return true;
            if (obj.GetType() != this.GetType()) return false;
            return Equals((Recipe) obj);
        }

        public override int GetHashCode()
        {
            return Id;
        }

        public static bool operator ==(Recipe left, Recipe right)
        {
            return Equals(left, right);
        }

        public static bool operator !=(Recipe left, Recipe right)
        {
            return !Equals(left, right);
        }
    }
}